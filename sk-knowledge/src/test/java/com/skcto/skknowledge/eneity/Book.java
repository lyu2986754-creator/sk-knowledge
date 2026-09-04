package com.skcto.skknowledge.eneity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Book {

    private String id;

    private String name;

    private String author;

    private BigDecimal price;

    private int totalPages;
}
