package za.co.eyetv.usersecurity.mapper;

import org.mapstruct.Mapper;
import za.co.eyetv.usersecurity.model.User;
import za.co.eyetv.usersecurity.model.dto.UserDTO;

/**
*@author: Rethabile Ntsekhe
*@date: @date: 09-04-2025
*/
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(User user);
    User toEntity(UserDTO userDTO);
}

