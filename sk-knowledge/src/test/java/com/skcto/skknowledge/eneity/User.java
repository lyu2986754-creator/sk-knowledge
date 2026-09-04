package com.skcto.skknowledge.eneity;

import com.drew.metadata.exif.PanasonicRawIFD0Descriptor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private Integer id;
    private String username;
}
