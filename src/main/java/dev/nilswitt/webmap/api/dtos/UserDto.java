package dev.nilswitt.webmap.api.dtos;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserDto extends AbstractEntityDto {
    private String username;
    private String email;
    private String firstName;
    private String lastName;

}
