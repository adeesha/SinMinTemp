/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.tika.metadata.DublinCore;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;

import crawler.Configurable;
import crawler.CrawlConfig;
import crawler.Page;
import org.apache.tika.parser.html.HtmlMapper;
import url.URLCanonicalizer;
import url.WebURL;
import util.Util;

/**
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */

class AllTagMapper implements HtmlMapper {
 
    @Override
    public String mapSafeElement(String name) {
        return name.toLowerCase();
    }

    @Override
    public boolean isDiscardElement(String name) {
        return false;
    }

    @Override
    public String mapSafeAttribute(String elementName, String attributeName) {
        return attributeName.toLowerCase();
    }

}
public class Parser extends Configurable {

	protected static final Logger logger = Logger.getLogger(Parser.class.getName());

	private HtmlParser htmlParser;
	private ParseContext parseContext;

	public Parser(CrawlConfig config) {
		super(config);
		htmlParser = new HtmlParser();
		parseContext = new ParseContext();
	}

	public boolean parse(Page page, String contextURL) throws InstantiationException, IllegalAccessException {

		if (Util.hasBinaryContent(page.getContentType())) {
			if (!config.isIncludeBinaryContentInCrawling()) {
				return false;
			}

			page.setParseData(BinaryParseData.getInstance());
			return true;

		} else if (Util.hasPlainTextContent(page.getContentType())) {
			try {
				TextParseData parseData = new TextParseData();
				parseData.setTextContent(new String(page.getContentData(), page.getContentCharset()));
				page.setParseData(parseData);
				return true;
			} catch (Exception e) {
				logger.error(e.getMessage() + ", while parsing: " + page.getWebURL().getURL());
			}
			return false;
		}

		Metadata metadata = new Metadata();
                 parseContext.set(HtmlMapper.class, AllTagMapper.class.newInstance());
		HtmlContentHandler contentHandler = new HtmlContentHandler();
		InputStream inputStream = null;
		try {
			inputStream = new ByteArrayInputStream(page.getContentData());
			htmlParser.parse(inputStream, contentHandler, metadata, parseContext);
		} catch (Exception e) {
			logger.error(e.getMessage() + ", while parsing: " + page.getWebURL().getURL());
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				logger.error(e.getMessage() + ", while parsing: " + page.getWebURL().getURL());
			}
		}

		if (page.getContentCharset() == null) {
			page.setContentCharset(metadata.get("Content-Encoding"));
		}

		HtmlParseData parseData = new HtmlParseData();
		parseData.setText(contentHandler.getBodyText().trim());
		parseData.setTitle(metadata.get(DublinCore.TITLE));

		List<WebURL> outgoingUrls = new ArrayList<>();

		String baseURL = contentHandler.getBaseUrl();
		if (baseURL != null) {
			contextURL = baseURL;
		}

		int urlCount = 0;
		for (ExtractedUrlAnchorPair urlAnchorPair : contentHandler.getOutgoingUrls()) {
			String href = urlAnchorPair.getHref();
			href = href.trim();
			if (href.length() == 0) {
				continue;
			}
			String hrefWithoutProtocol = href.toLowerCase();
			if (href.startsWith("http://")) {
				hrefWithoutProtocol = href.substring(7);
			}
			if (!hrefWithoutProtocol.contains("javascript:") 
					&& !hrefWithoutProtocol.contains("mailto:")
					&& !hrefWithoutProtocol.contains("@")) {
				String url = URLCanonicalizer.getCanonicalURL(href, contextURL);
				if (url != null) {
					WebURL webURL = new WebURL();
					webURL.setURL(url);
					webURL.setAnchor(urlAnchorPair.getAnchor());
					outgoingUrls.add(webURL);
					urlCount++;
					if (urlCount > config.getMaxOutgoingLinksToFollow()) {
						break;
					}
				}
			}
		}

		parseData.setOutgoingUrls(outgoingUrls);

		try {
			if (page.getContentCharset() == null) {
				parseData.setHtml(new String(page.getContentData()));
			} else {
				parseData.setHtml(new String(page.getContentData(), page.getContentCharset()));
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}

		page.setParseData(parseData);
		return true;

	}

}
