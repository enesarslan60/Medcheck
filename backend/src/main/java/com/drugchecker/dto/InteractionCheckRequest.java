package com.drugchecker.dto;

import com.drugchecker.validation.ValidDrugName;
import com.drugchecker.validation.ValidDrugNameList;
import lombok.Data;

import java.util.List;

@Data
public class InteractionCheckRequest {

    @ValidDrugNameList
    private List<@ValidDrugName String> drugNames;
}
