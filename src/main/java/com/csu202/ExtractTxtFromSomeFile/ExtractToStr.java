 /** 
 * Project Name:ExtractContentFromSomeFile 
 * File Name:ExtractToStr.java 
 * Package Name:com.csu202.ExtractContentFromSomeFile 
 * Date:2018年3月3日下午7:26:24 
 * Copyright (c) 2018, taoge@tmd.me All Rights Reserved. 
 * 
 */  
  
package com.csu202.ExtractTxtFromSomeFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

/** 
 * 目前适用文件有txt，doc，docx，pdf
 * 可能部分文件提取出错
 * ClassName:ExtractToStr <br/> 
 * Function: TODO ADD FUNCTION. <br/> 
 * Reason:   TODO ADD REASON. <br/> 
 * Date:     2018年3月3日 下午7:26:24 <br/> 
 * @author   Administrator 
 * @version   
 * @since    JDK 1.8 
 * @see       
 */
public class ExtractToStr {
	private static final String pageStart = "--pageStart--";
	private static final String paraStart = "--paraStart--";
	
	/**
	 * 提取出文档中的文本内容,适用文件类型txt，doc，docx，pdf
	 * 
	 * @return String
	 * @throws Exception
	 */
	public String extractToString(String path) throws Exception{
		String txt=null;
		// 得到文件的类型，判断pdf文档或者word文档，分别提取内容
		String type = path.substring(path.lastIndexOf(".") + 1);
		if (type.trim().equals("pdf")) {
			txt = getTxtfromPdf(path);
			txt = TxtAnalyse.dealPdfContent(txt);
		} else if (type.trim().equals("doc") || type.trim().equals("docx")) {
			 txt=getTxtfromWord(path);
		} else if (type.trim().equals("txt")) {
			txt = getTxtfromTxt(path);
		} else{
			return null;
		}

		// 消除乱码
		Pattern pattern = Pattern.compile("[<>&'\"\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f\\uD800-\\uDFFF]");
		Matcher matcher = pattern.matcher(txt);
		matcher.replaceAll("");
		if(txt!=null) {
			txt = TxtAnalyse.ReplaceLowOrderASCIICharacters(txt);
		}
		return txt;
	}

	private static String getTxtfromTxt(String filepath) throws Exception {
		String text = null;
		try {
			text = parseFile(filepath);
		}catch(Exception e){
			try {
				Tika tika = new Tika();
				tika.setMaxStringLength(999999999);
				text = tika.parseToString(new File(filepath));
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return text;
//		 InputStreamReader reader=new InputStreamReader(new
////		 FileInputStream(filepath),"UTF-8");
//				 FileInputStream(filepath));
//		 BufferedReader br=new BufferedReader(reader);
//		 String lineTxt = null;
//		 while((lineTxt = br.readLine()) != null){
//			 result+=lineTxt;
//		 }
//		 reader.close();
//		return result;
	}

	/*
	 * 引用pdfbox.jar中的方法处理pdf文档，获得返回内容
	 */
	private String getTxtfromPdf(String filepath) throws Exception {
		System.setProperty("org.apache.pdfbox.baseParser.pushBackSize", new Long(100*1024*1024).toString());
		PDDocument doc = null;
		String result = "";

		try {
			FileInputStream fileInputStream = new FileInputStream(filepath);
			PDFParser parser = new PDFParser(fileInputStream);
			parser.parse();
			doc = parser.getPDDocument();
			if (doc.isEncrypted()) {
				return null;
			}
			PDFTextStripper stripper = new PDFTextStripper();
			stripper.setPageEnd(pageStart);
			stripper.setParagraphStart(paraStart);
			result = stripper.getText(doc);

		} catch (Exception e) {
			try {
				result = parseFile(filepath);
			} catch (Exception e1) {
				try {
					Tika tika = new Tika();
					tika.setMaxStringLength(999999999);
					result = tika.parseToString(new File(filepath));
				} catch (Exception e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
			}
		} finally {
			if (doc != null) {
				try {
					doc.close();
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				} finally {
					doc = null;
				}
			}
		}

		return result;
	}
	
	private String getTxtfromWord(String filepath)throws Exception{
		String text = null;
		try {
			text = parseFile(filepath);
		}catch(Exception e){
			try {
				Tika tika = new Tika();
				tika.setMaxStringLength(999999999);
				text = tika.parseToString(new File(filepath));
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return text;
	}

	private static String parseFile(String filepath) {
		File file = new File(filepath);
		Parser parser = new AutoDetectParser();
		InputStream input = null;
		try {
			Metadata metadata = new Metadata();
			metadata.set(Metadata.CONTENT_ENCODING, "utf-8");
			metadata.set(Metadata.RESOURCE_NAME_KEY, file.getName());
			input = new FileInputStream(file);
			ContentHandler handler = new BodyContentHandler(1024*1024*1024);//当文件大于100000时，new BodyContentHandler(1024*1024*10);
			ParseContext context = new ParseContext();
			context.set(Parser.class, parser);
			parser.parse(input, handler, metadata, context);
//			for (String name : metadata.names()) {
//				System.out.println(name + ":" + metadata.get(name));
//			}
//			System.out.println(handler.toString());
//			System.out.println();
			return handler.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (input != null) input.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;

	}

}
  