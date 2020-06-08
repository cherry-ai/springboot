package com.summit.coordinates.controller;

import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.summit.coordinates.common.ExcelListener;
import com.summit.coordinates.common.MyCoordinate;
import com.summit.coordinates.util.CoordinateConvertUtils;
import com.summit.coordinates.util.CoordinateFormatUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by liusj on 2019/5/24
 */
@Slf4j
@Api(value = "Excel批量转换坐标", tags = "Excel批量转换坐标")
@RestController("/batch")
public class IndexController {

    private static final String REGEX = "^\\d+" + "°" + "\\d+" + "′" + "\\d*\\.?\\d*" + "″";
    private static final String REGEX2 = "^\\d+" + "°" + "\\d*\\.?\\d*" + "′";
    private static final String REGEX3 = "^\\d+" + "(\\." + "\\d+)?$";

    @Autowired
    private CacheManager cacheManager;

    //    @GetMapping("/")
    public String index() {
        return "index";
    }

    @ApiOperation("转换后的坐标Excel导出 (复制请求链接浏览器访问下载)")
    @GetMapping("/export")
    public void downLoad(HttpServletResponse response) throws Exception {
        Cache cache = cacheManager.getCache("file");
        Cache.ValueWrapper cacheValue = cache.get("result");
        if (cacheValue != null) {
            List<MyCoordinate> result = (List<MyCoordinate>) cacheValue.get();
            //  写入excel
            response.setCharacterEncoding("UTF-8");
            String name = URLEncoder.encode("火星坐标.xlsx", "UTF-8");
            response.setContentType("application/x-msdownload");
            response.addHeader("Content-Disposition", "attachment;filename*=utf-8'zh_cn'" + name);
            try (OutputStream out = response.getOutputStream()) {
                ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX);
                Sheet sheet1 = new Sheet(1, 0, MyCoordinate.class);

                sheet1.setSheetName("转换后的火星坐标");
                writer.write(result, sheet1);
                writer.finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cache.clear();
    }

    @ApiOperation("Excel批量导入坐标")
    @PostMapping("/import")
    public void importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        // 清缓存
        Cache cache = cacheManager.getCache("file");
        cache.clear();

        // 解析每行结果在listener中处理
        ExcelListener listener = new ExcelListener();
        ExcelReader excelReader = new ExcelReader(file.getInputStream(), ExcelTypeEnum.XLSX, null, listener);
        excelReader.read(new Sheet(1, 2, MyCoordinate.class));

        List<MyCoordinate> datas = listener.getDatas();

        // 数据转化
        Pattern pattern = Pattern.compile(REGEX);
        Pattern pattern2 = Pattern.compile(REGEX2);
        Pattern pattern3 = Pattern.compile(REGEX3);

        List<MyCoordinate> result = new ArrayList<>();
        String lat;
        String lng;
        for (MyCoordinate myCoordinate : datas) {

            lat = myCoordinate.getLatitude().trim();
            lng = myCoordinate.getLongitude().trim();

            if (StringUtils.isEmpty(lat) || StringUtils.isEmpty(lng)) {
                myCoordinate.setRemark("数据不能为空");
                result.add(myCoordinate);
                continue;
            }
            if (pattern.matcher(lat).find() && pattern2.matcher(lng).find()) {
                myCoordinate.setLongitude(CoordinateFormatUtils.DmsTurnDD(lat));
                myCoordinate.setLatitude(CoordinateFormatUtils.DmsTurnDD(lng));
                result.add(CoordinateConvertUtils.wgs84ToGcj02Copy(myCoordinate));
                continue;
            }
            if (pattern2.matcher(lat).find() && pattern2.matcher(lng).find()) {
                myCoordinate.setLongitude(CoordinateFormatUtils.DmTurnDD(lat));
                myCoordinate.setLatitude(CoordinateFormatUtils.DmTurnDD(lng));
                result.add(CoordinateConvertUtils.wgs84ToGcj02Copy(myCoordinate));
                continue;
            }
            if (pattern3.matcher(lat).find() && pattern3.matcher(lng).find()) {
                result.add(CoordinateConvertUtils.wgs84ToGcj02Copy(myCoordinate));
                continue;
            }
            myCoordinate.setRemark("数据格式有误");
            result.add(myCoordinate);
        }
        //  写入緩存
        cache.put("result", result);
    }
}
