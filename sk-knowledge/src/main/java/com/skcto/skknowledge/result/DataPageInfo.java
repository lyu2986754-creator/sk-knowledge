package com.skcto.skknowledge.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DataPageInfo<T> {

    //总记录数
    private long itemCount;
    private long pageSize;
    private long page;
    private List<T> list;

    public DataPageInfo(long itemCount, long pageSize, long page, List<T> list) {
        this.itemCount = itemCount;
        this.pageSize = pageSize;
        this.page = page;
        this.list = list;
    }

    public static <T> DataPageInfo<T> build(IPage<T> page) {
        DataPageInfo<T> dataPageInfo = new DataPageInfo<>();
        dataPageInfo.setPageSize(page.getSize());
        dataPageInfo.setPage(page.getCurrent());
        dataPageInfo.setItemCount(page.getTotal());
        dataPageInfo.setList(page.getRecords());
        return dataPageInfo;
    }
}
