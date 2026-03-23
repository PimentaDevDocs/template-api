package com.pimentadesenvolvimento.mapper;

import com.pimentadesenvolvimento.domain.User;
import com.pimentadesenvolvimento.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {RoleMapper.class, PersonMapper.class})
public interface UserMapper {

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "roles", target = "roles")
    @Mapping(source = "person", target = "person")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "createdBy", target = "createdBy")
    UserResponse toResponse(User entity);
}