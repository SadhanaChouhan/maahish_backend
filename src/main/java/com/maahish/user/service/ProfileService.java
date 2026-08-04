package com.maahish.user.service;

import com.maahish.user.dto.request.DeleteAccountRequest;

public interface ProfileService {

    void deleteAccount(Long userId, DeleteAccountRequest request);
}
