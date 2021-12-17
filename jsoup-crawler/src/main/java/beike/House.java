package beike;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * TODO
 *
 * @author suwei
 * @version 1.0
 * @date 2021/12/16 17:05
 */
@Data
@Accessors(chain = true)
public class House {
    @ExcelProperty("楼盘名称")
    private String title;

    @ExcelProperty("访问网页")
    private String detailPageUrl;

    @ExcelProperty("楼盘图片")
    private String imageUrl;

    @ExcelProperty("所在地址")
    private String address;

    @ExcelProperty("户型")
    private String houseType;

    @ExcelProperty("房产类型")
    private String propertyType;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("建筑面积")
    private String buildingArea;

    @ExcelProperty("总价")
    private String totalPrice;

    @ExcelProperty("单价（元/㎡(均价)）")
    private String singlePrice;

    @ExcelProperty("标签")
    private String tag;
}
