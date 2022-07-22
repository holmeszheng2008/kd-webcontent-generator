package com.studio.eric.service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Description {
    private String project_name;
    private String build_office;
    private String observation_period;
    private String observation_office;
    private String observation_staff;
    private String filing_date;
}
