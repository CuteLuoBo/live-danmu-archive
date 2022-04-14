package com.github.cuteluobo.livedanmuarchive;

import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.qq.tars.protocol.tars.TarsInputStream;
import com.qq.tars.protocol.tars.TarsOutputStream;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
class ApplicationTests {


    void testHuya() throws ServiceException, IOException, InterruptedException, URISyntaxException {

    }

    public void socketTest() throws URISyntaxException, IOException, InterruptedException {
        Pattern TT_META_DATA = Pattern.compile("TT_META_DATA(\\S)+}", Pattern.MULTILINE);
        Pattern YYID =Pattern.compile("Yyid\":([0-9]+)", Pattern.MULTILINE);
        Pattern TID =Pattern.compile("ChannelId\":([0-9]+)", Pattern.MULTILINE);
        Pattern SID  =Pattern.compile("SubChannelId\":([0-9]+)", Pattern.MULTILINE);
        HttpClient.Builder clientBuilder = HttpClient.newBuilder();
//        clientBuilder.proxy(ProxySelector.getDefault());
        //连接超时时间60s
        clientBuilder.connectTimeout(Duration.ofSeconds(60));
        HttpRequest httpRequest = HttpRequest.newBuilder(new URI("https://m.huya.com/712416")).GET()
                .setHeader("user-agent","Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML," +
                        "like Gecko) Chrome/79.0.3945.88 Mobile Safari/537.36").build();
        HttpClient httpClient = clientBuilder.build();
        //获取返回数据
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        //获取返回网页信息字符串
        String body = httpResponse.body();
        Matcher yyidMatcher = YYID.matcher(body);
        int ayyuid = Integer.parseInt(yyidMatcher.group(1));
        Matcher tidMatcher = TID.matcher(body);
        int tid = Integer.parseInt(tidMatcher.group(1));
        Matcher sidMatcher = SID.matcher(body);
        int sid = Integer.parseInt(sidMatcher.group(1));
        TarsOutputStream tarsOutputStream = new TarsOutputStream();
        //写入解析模式
        tarsOutputStream.write(ayyuid, 0);
        tarsOutputStream.write(Boolean.TRUE, 1);
        tarsOutputStream.write("", 2);
        tarsOutputStream.write("", 3);
        tarsOutputStream.write(tid, 4);
        tarsOutputStream.write(sid, 5);
        tarsOutputStream.write(0, 6);
        tarsOutputStream.write(0, 7);

        //ws解析指令
        TarsOutputStream websocketCmd = new TarsOutputStream();
        websocketCmd.write(1, 0);
        websocketCmd.write(tarsOutputStream.getByteBuffer(),1);

        TarsInputStream tarsInputStream = new TarsInputStream();
        tarsInputStream.read(false, 0, false);
//        WebSocketClient webSocketClient = new BaseWebSocketClient(new URI("wss://cdnws.api.huya.com/"), null, 60, danMuParseService);
//        webSocketClient.connect();
    }
}
