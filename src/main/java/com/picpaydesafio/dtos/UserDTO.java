package com.picpaydesafio.dtos;

import com.picpaydesafio.domain.user.UserType;

import java.math.BigDecimal;

public record UserDTO(String firstName, String lastName, String email, String password, String document,
                      UserType userType, BigDecimal balance) {
}
