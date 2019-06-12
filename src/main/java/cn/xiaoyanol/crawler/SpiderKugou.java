package cn.xiaoyanol.crawler;

/**
 * @Auther: chenrj
 * @Date: 2019/6/12 14:28
 * @Description:
 */
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.xiaoyanol.crawler.html.HtmlManage;
import cn.xiaoyanol.crawler.utils.FileDownload;
import cn.xiaoyanol.crawler.utils.HttpGetConnect;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.alibaba.fastjson.JSONObject;


//参考 https://blog.csdn.net/qq_40663357/article/details/89469770
//

public class SpiderKugou {


    private static Log log = LogFactory.getLog(SpiderKugou.class);

    public static String filePath = "/Users/a123/Downloads/music/";

    public  static Map map =new HashMap();

    /*public static String mp3 = "https://wwwapi.kugou.com/yy/index.php?r=play/getdata&callback=jQuery191027067069941080546_1546235744250&"
            + "hash=HASH&album_id=0&_=TIME";*/

    public static String mp3  = "https://wwwapi.kugou.com/yy/index.php?r=play/getdata&callback=jQuery19108318842169864549_1558160178250&hash=HASH&album_id=ALBUMID&dfid=4Vyhka0JsPzT0DLMy10TfJPj&mid=122dc1e8e26152d6ec1aca669ca448d3&platid=4&_=TIME";


    //https://wwwapi.kugou.com/yy/index.php?r=play/getdata&callback=jQuery191001526381364107765_1560321877251
    // &hash=7995A2173ED0914868BB860F93C3D642
    // &album_id=20709823
    // &dfid=07CDmO1HuZxn0PC4Vt0UxlpJ
    // &mid=d4910a5ca5df086bef69c19436d7598a
    // &platid=4
    // &_=1560321877253


    public static String LINK = "https://www.kugou.com/yy/rank/home/PAGE-8888.html?from=rank";

    //"https://www.kugou.com/yy/rank/home/PAGE-23784.html?from=rank";


    public static void main(String[] args) throws IOException {

        for(int i = 1 ; i < 5 ; i++){
            String url = LINK.replace("PAGE", i + "");
            getTitle(url);
            //download("https://www.kugou.com/song/mfy6je5.html");
        }
    }

    public static String getTitle(String url) throws IOException{
        HttpGetConnect connect = new HttpGetConnect();
        String content = connect.connect(url, "utf-8");
        HtmlManage html = new HtmlManage();
        Document doc = html.manage(content);


        int beginIdx = content.indexOf("global.features = ");
        int endIdx = content.indexOf("];", beginIdx);
        String features = content.substring(beginIdx, endIdx + 1).replace("global.features = ", "");
        //log.info("containingOwnText >>>>>> " + features);

        List<JSONObject> list = JSONObject.parseArray(features, JSONObject.class);
        for (JSONObject jsonObject : list) {
            String hash = (String) jsonObject.get("Hash");
            Integer albumId = (Integer) jsonObject.get("album_id");
           // EhcacheUtil.setCache(hash, albumId);
            map.put(hash, albumId);
        }


        Element ele = doc.getElementsByClass("pc_temp_songlist").get(0);
        Elements eles = ele.getElementsByTag("li");
        for(int i = 0 ; i < eles.size() ; i++){
            Element item = eles.get(i);
            String title = item.attr("title").trim();
            String link = item.getElementsByTag("a").first().attr("href");

            download(link,title);
        }
        return null;
    }

    public static String download(String url,String name) throws IOException{
        String hash = "";
        HttpGetConnect connect = new HttpGetConnect();
        String content = connect.connect(url, "utf-8");
        HtmlManage html = new HtmlManage();

        String regEx = "\"hash\":\"[0-9A-Z]+\"";
        // 编译正则表达式
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            hash = matcher.group();
            hash = hash.replace("\"hash\":\"", "");
            hash = hash.replace("\"", "");
        }


        mp3 = mp3.replace("ALBUMID", map.get(hash).toString());
        String item = mp3.replace("HASH", hash);
        item = item.replace("TIME", System.currentTimeMillis() + "");

        System.out.println(item);
        String mp = connect.connect(item, "utf-8");

        mp = mp.substring(mp.indexOf("(") + 1, mp.length() - 3);

        JSONObject json = JSONObject.parseObject(mp);
        String playUrl = json.getJSONObject("data").getString("play_url");


        System.out.print(playUrl + "  ==  ");
        FileDownload down = new FileDownload();
        down.download(playUrl, filePath + name + ".mp3");

        System.out.println(name + "下载完成");
        return playUrl;


    }

}