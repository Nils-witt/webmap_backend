package dev.nilswitt.webmap.api.dtos;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class SecurityGroupDto extends AbstractEntityDto {
    private String name;
    private String ssoGroupName;
    private List<String> roles;
}
