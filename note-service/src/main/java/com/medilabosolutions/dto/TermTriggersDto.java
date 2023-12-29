package com.medilabosolutions.dto;

import java.util.List;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class TermTriggersDto {

    @Size.List (
        @Size(min=1,message = "the list of term triggers must be not blank and contains atleast one string")
    )
    private List<String> termTriggers;

}
