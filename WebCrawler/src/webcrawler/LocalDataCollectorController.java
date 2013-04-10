/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcrawler;

/**
 *
 * @author adeesha
 */
import java.util.List;

import crawler.CrawlConfig;
import crawler.CrawlController;
import fetcher.PageFetcher;
import robotstxt.RobotstxtConfig;
import robotstxt.RobotstxtServer;

public class LocalDataCollectorController {
      public static void main(String[] args) throws Exception {
//                if (args.length != 2) {
//                        System.out.println("Needed parameters: ");
//                        System.out.println("\t rootFolder (it will contain intermediate crawl data)");
//                        System.out.println("\t numberOfCralwers (number of concurrent threads)");
//                        return;
//                }
                String rootFolder = "projectdata";
                int numberOfCrawlers = 7;

                CrawlConfig config = new CrawlConfig();
                config.setCrawlStorageFolder(rootFolder);
                config.setMaxPagesToFetch(10);
                config.setPolitenessDelay(1000);

                PageFetcher pageFetcher = new PageFetcher(config);
                RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
                RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
                CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

                controller.addSeed("http://www.lankadeepa.lk/");
                controller.start(LocalDataCollectorCrawler.class, numberOfCrawlers);

                List<Object> crawlersLocalData = controller.getCrawlersLocalData();
                long totalLinks = 0;
                long totalTextSize = 0;
                int totalProcessedPages = 0;
                for (Object localData : crawlersLocalData) {
                        CrawlStat stat = (CrawlStat) localData;
                        totalLinks += stat.getTotalLinks();
                        totalTextSize += stat.getTotalTextSize();
                        totalProcessedPages += stat.getTotalProcessedPages();
                }
                System.out.println("Aggregated Statistics:");
                System.out.println("   Processed Pages: " + totalProcessedPages);
                System.out.println("   Total Links found: " + totalLinks);
                System.out.println("   Total Text Size: " + totalTextSize);
        }
}
