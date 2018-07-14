import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import spark.Spark;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class App {
    public static void main(String[] args) throws IOException {
        Spark.get("/*", (req, res) -> {

            String requestedArticle = req.splat()[0];

            Document doc = Jsoup.connect("http://en.wikipedia.org/wiki/" + requestedArticle).get();

            //return renderByHand(doc);
            return renderWithMustache(doc);

        });
    }

    private static String renderWithMustache(Document doc) throws IOException {
        //http://www.baeldung.com/mustache

        //create mustache factory and compile the template
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache m = mf.compile("template.mustache");


        //create a list of link values
        List<LinkValues> linkMaps = new ArrayList<>();
        Elements elements = doc.select("#mw-content-text a");
        Iterator it = elements.iterator();
        while(it.hasNext()){
            Element e = (Element)it.next();
            linkMaps.add(new LinkValues(e.attr("href"), e.text()));
        }


        Map<String, Object> values = new HashMap<>();
        values.put("text", findFirstParagraphText(doc));
        values.put("links", linkMaps);

        //use the compiled template to get the HTML
        StringWriter writer = new StringWriter();
        m.execute(writer, values).flush();
        String html = writer.toString();

        return html;
    }

    private static String renderByHand(Document doc) {
        String result = "";

        result += findFirstParagraphText(doc);

        Elements allLinks = doc.select("a");

        result += renderLinks(allLinks);
        //result += renderLinksGroupedByFirstChar(allLinks);
        return result;
    }

    private static String findFirstParagraphText(Document doc) {
        Elements paragraf = doc.select("#mw-content-text p");
        Element first = paragraf.first();
        if(first==null){
            return "Empty paragraf";
        }

        return first.text();
    }

    private static String renderLinks(Elements allLinks) {
        return "";// allLinks.stream()
        // keep only the links with a non-empty text
        //TODO
        //filter to have text !=null and text != empty
        //filter to have href starting with "/wiki"
        //sort by text - optional

        // transform from Element instances to
        // strings with <a> elements
        //"<br><a href=\""
        //        + link
        //        + "\">"
        //        + link.text
        //        + "</a>")
        // this merges the stream of strings into a single string
    }

    private static String renderLinksGroupedByFirstChar(Elements allLinks) {
        String result = "";
        Map<Character, List<Element>> linksByCharacter = allLinks.stream()
                // keep only the links with a non-empty text
                .filter(a -> a.text() != null && !a.text().isEmpty())
                .filter(a -> a.attr("href").startsWith("/wiki/"))
                .collect(Collectors.groupingBy(a -> a.text().charAt(0)));

        for(Map.Entry<Character, List<Element>> mapEntry : linksByCharacter.entrySet()) {

            result += "<h1 style='font-family: Arial'>" + mapEntry.getKey() + "</h1>";

            result += mapEntry.getValue().stream()
                    .map(link -> "<br><a href=\""
                            + link.attr("href")
                            + "\">"
                            + link.text()
                            + "</a>")
                    // this merges the stream of strings into a single string
                    .collect(Collectors.joining());
        }
        ;
        return result;
    }

}