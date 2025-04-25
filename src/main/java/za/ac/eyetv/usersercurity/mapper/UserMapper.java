package za.ac.eyetv.usersercurity.mapper;

import org.mapstruct.Mapper;
import za.ac.eyetv.usersercurity.model.User;
import za.ac.eyetv.usersercurity.model.dto.UserDTO;

/**
*@author: Rethabile Ntsekhe
*@date: @date: 09-04-2025
*/
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(User user);
    User toEntity(UserDTO userDTO);
}

