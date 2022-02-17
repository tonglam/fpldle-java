package com.tong.fpl.controller;

import com.tong.fpl.api.IFpldleApi;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.FpldleData;
import com.tong.fpl.domain.FpleResultData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Create by tong on 2022/2/12
 */
@RestController
@RequestMapping("/fpldle")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FpldleController {

    private final IFpldleApi fpldleApi;

    @GetMapping("/getDailyFpldle")
    public FpldleData getDailyFpldle() {
        return this.fpldleApi.getDailyFpldle(LocalDate.now().format(DateTimeFormatter.ofPattern(Constant.SHORTDAY)));
    }

    @GetMapping("/getWechatUserOpenId")
    public String getWechatUserOpenId(@RequestParam String code) {
        return this.fpldleApi.getWechatUserOpenId(code);
    }

    @GetMapping("/fuzzyQueryName")
    public List<String> fuzzyQueryName(@RequestParam String fuzzyName) {
        return this.fpldleApi.fuzzyQueryName(fuzzyName);
    }

    @RequestMapping("/insertDailyResult")
    @ResponseBody
    public void insertDailyResult(@RequestBody FpleResultData fpleResultData) {
        this.fpldleApi.insertDialyResult(fpleResultData.getOpenId(), fpleResultData.getResultList());
    }

    @GetMapping("/getDailyResult")
    public List<String> getDailyResult(@RequestParam String openId) {
        return this.fpldleApi.getDailyResult(openId);
    }

    @GetMapping("/getPlayerPicture")
    public void getPlayerPicture(@RequestParam int code, HttpServletResponse response) {
        InputStream inputStream;
        OutputStream outputStream;
        try (OutputStream ignored = outputStream = response.getOutputStream()) {
            inputStream = this.fpldleApi.getPlayerPicture(code);
            int bytesum = 0;
            int byteread;
            // 清空response
            response.reset();
            // 设置response的Header
            if (inputStream != null) {
                byte[] buffer = new byte[1024];
                while ((byteread = inputStream.read(buffer)) != -1) {
                    bytesum += byteread;
                    outputStream.write(buffer, 0, byteread);
                }
            }
            response.addHeader("Content-Length", "" + bytesum);
            response.setContentType("image/png");
            outputStream.flush();
            Objects.requireNonNull(inputStream).close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
