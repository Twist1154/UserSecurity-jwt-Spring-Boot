package za.co.eyetv.usersecurity.common.util;

import org.mapstruct.Mapper;
import za.co.eyetv.usersecurity.user.model.User;
import za.co.eyetv.usersecurity.user.dto.UserDTO;

/**
*@author: Rethabile Ntsekhe
*@date: @date: 09-04-2025
*/
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(User user);
    User toEntity(UserDTO userDTO);
}

