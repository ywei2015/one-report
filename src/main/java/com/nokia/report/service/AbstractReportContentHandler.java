package com.nokia.report.service;


import org.apache.poi.ooxml.POIXMLDocument;

import java.util.Map;

abstract class AbstractReportContentHandler {
	abstract void renderContent(Map<String,Object> data, POIXMLDocument slideShow);
}
