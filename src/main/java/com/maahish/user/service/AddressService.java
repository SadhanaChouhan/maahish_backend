package com.maahish.user.service;

import com.maahish.user.dto.request.AddressRequest;
import com.maahish.user.dto.response.AddressResponse;


import java.util.List;

public interface AddressService {

    List<AddressResponse> getUserAddresses(Long userId);

    AddressResponse createAddress(Long userId, AddressRequest request);

    AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request);

    void deleteAddress(Long userId, Long addressId);

    AddressResponse setDefault(Long userId, Long addressId);
}
