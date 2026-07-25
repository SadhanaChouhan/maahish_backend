package com.maahish.service.impl;

import com.maahish.dto.request.AddressRequest;
import com.maahish.dto.response.AddressResponse;
import com.maahish.entity.Address;
import com.maahish.entity.User;
import com.maahish.exception.ResourceNotFoundException;
import com.maahish.location.LocationService;
import com.maahish.mapper.OrderMapper;
import com.maahish.repository.AddressRepository;
import com.maahish.repository.UserRepository;
import com.maahish.security.ShoppingAccessValidator;
import com.maahish.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final LocationService locationService;
    private final ShoppingAccessValidator shoppingAccessValidator;

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(Long userId) {
        shoppingAccessValidator.requireCustomer(userId);
        User user = getUser(userId);
        return addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user).stream()
                .map(orderMapper::toAddressResponse)
                .toList();
    }

    @Override
    @Transactional
    public AddressResponse createAddress(Long userId, AddressRequest request) {
        shoppingAccessValidator.requireCustomer(userId);
        User user = getUser(userId);
        if (Boolean.TRUE.equals(request.getIsDefault()) || addressRepository.countByUser(user) == 0) {
            clearDefault(user);
        }
        Address address = mapToEntity(new Address(), request);
        address.setUser(user);
        address.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : addressRepository.countByUser(user) == 0);
        return orderMapper.toAddressResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request) {
        shoppingAccessValidator.requireCustomer(userId);
        User user = getUser(userId);
        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefault(user);
        }
        mapToEntity(address, request);
        return orderMapper.toAddressResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        shoppingAccessValidator.requireCustomer(userId);
        User user = getUser(userId);
        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public AddressResponse setDefault(Long userId, Long addressId) {
        shoppingAccessValidator.requireCustomer(userId);
        User user = getUser(userId);
        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        clearDefault(user);
        address.setIsDefault(true);
        return orderMapper.toAddressResponse(addressRepository.save(address));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void clearDefault(User user) {
        addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user).forEach(a -> {
            a.setIsDefault(false);
            addressRepository.save(a);
        });
    }

    private Address mapToEntity(Address address, AddressRequest request) {
        if (request.getCountryCode() != null) {
            address.setCountryCode(request.getCountryCode());
            address.setCountry(locationService.resolveCountryName(request.getCountryCode()));
        } else if (request.getCountry() != null) {
            address.setCountry(request.getCountry());
        }
        address.setFullName(request.getFullName());
        address.setMobile(request.getMobile());
        address.setAlternateMobile(blankToNull(request.getAlternateMobile()));
        address.setPincode(request.getPincode());
        address.setState(request.getState());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setAddressLine(request.getAddressLine());
        address.setLandmark(request.getLandmark());
        if (request.getIsDefault() != null) {
            address.setIsDefault(request.getIsDefault());
        }
        return address;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
