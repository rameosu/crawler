package beike;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 利用Jsoup爬取贝壳网-深圳的所有楼盘信息
 *
 * @author suwei
 * @version 1.0
 * @date 2021/12/16 14:02
 */
@Slf4j
public class BeikeJsoup {
    @SneakyThrows
    public static void main(String[] args) {
        AtomicInteger pageIndex = new AtomicInteger(1);
        int pageSize = 10;
        List<House> dataList = Lists.newArrayList();

        // 贝壳找房深圳区域网址
        String beikeUrl = "https://sz.fang.ke.com";

        // 贝壳找房深圳市楼盘展示页地址
        String loupanUrl = "https://sz.fang.ke.com/loupan/pg";

        // 用Jsoup抓取该地址完整网页信息
        Document doc = Jsoup.connect(loupanUrl + pageIndex.get()).get();

        // 网页标题
        String pageTitle = doc.title();

        // 分页容器
        Element pageContainer = doc.select("div.page-box").first();
        if (pageContainer == null) {
            return;
        }
        // 楼盘总数
        int totalCount = Integer.parseInt(pageContainer.attr("data-total-count"));
        for (int i = 0; i < totalCount / pageSize; i++) {
            log.info("running get data, the current page is {}", pageIndex.get());
            // 贝壳网有人机认证，不能短时间频繁访问，每次翻页都让线程休眠10s
            Thread.sleep(10000);
            doc = Jsoup.connect(loupanUrl + pageIndex.getAndIncrement()).get();

            // 获取楼盘列表的ul元素
            Element list = doc.select("ul.resblock-list-wrapper").first();
            if (list == null) {
                continue;
            }

            // 获取楼盘列表的li元素
            Elements elements = list.select("li.resblock-list");
            elements.forEach(el -> {

                // 楼盘介绍
                Element introduce = el.child(0);

                // 详情页面
                String detailPageUrl = beikeUrl + introduce.attr("href");

                // 楼盘图片
                String imageUrl = introduce.select("img").attr("data-original");

                // 楼盘详情
                Element childDesc = el.select("div.resblock-desc-wrapper").first();
                Element childName = childDesc.child(0);

                // 楼盘名称
                String title = childName.child(0).text();

                // 楼盘在售状态
                String status = childName.child(1).text();

                // 产权类型
                String propertyType = childName.child(2).text();

                // 楼盘所在地址
                String address = childDesc.child(1).text();

                // 房间属性
                Element room = childDesc.child(2);

                // 户型
                String houseType = "";

                // 户型集合
                Elements houseTypeSpans = room.getElementsByTag("span");
                if (CollectionUtils.isNotEmpty(houseTypeSpans)) {
                    // 剔除文案：【户型：】
                    houseTypeSpans.remove(0);
                    // 剔除文案：【建面：xxx】
                    houseTypeSpans.remove(houseTypeSpans.size() - 1);
                    houseType = StringUtil.join(houseTypeSpans.stream().map(Element::text).collect(Collectors.toList()), "/");
                }

                // 建筑面积
                String buildingArea = room.select("span.area").text();

                // div - 标签
                Element descTag = childDesc.select("div.resblock-tag").first();
                Elements tagSpans = descTag.getElementsByTag("span");
                String tag = "";
                if (CollectionUtils.isNotEmpty(tagSpans)) {
                    tag = StringUtil.join(tagSpans.stream().map(Element::text).collect(Collectors.toList()), " ");
                }

                // div - 价格
                Element descPrice = childDesc.select("div.resblock-price").first();
                String singlePrice = descPrice.select("span.number").text();
                String totalPrice = descPrice.select("div.second").text();

                dataList.add(new House().setTitle(title)
                        .setDetailPageUrl(detailPageUrl)
                        .setImageUrl(imageUrl)
                        .setSinglePrice(singlePrice)
                        .setTotalPrice(totalPrice)
                        .setStatus(status)
                        .setPropertyType(propertyType)
                        .setAddress(address)
                        .setHouseType(houseType)
                        .setBuildingArea(buildingArea)
                        .setTag(tag)
                );
            });
        }
        if (CollectionUtils.isEmpty(dataList)) {
            log.info("dataList is empty returned.");
            return;
        }
        log.info("dataList prepare finished, size = {}", dataList.size());
        export(pageTitle, dataList);
    }

    /**
     * 将爬取的数据写入到excel中
     * @param pageTitle
     * @param dataList
     */
    private static void export(String pageTitle, List<House> dataList) {
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        //设置头居中
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        //内容策略
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        //设置 水平居中
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
        HorizontalCellStyleStrategy horizontalCellStyleStrategy = new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
        // 这里需要设置不关闭流
        EasyExcelFactory.write("D:\\szlp.xlsx", House.class).autoCloseStream(Boolean.FALSE).registerWriteHandler(horizontalCellStyleStrategy).sheet(pageTitle).doWrite(dataList);
    }
}
