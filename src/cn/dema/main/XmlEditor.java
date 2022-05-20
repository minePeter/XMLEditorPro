package cn.dema.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import cn.dema.chunk.AttributeData;
import cn.dema.chunk.EndTagChunk;
import cn.dema.chunk.StartTagChunk;
import cn.dema.chunk.StringChunk;
import cn.dema.chunk.ResourceChunk;
import cn.dema.chunk.Chunk;

public class XmlEditor {
	
	public static int tagStartChunkOffset = 0, tagEndChunkOffset;
	public static int subAppTagChunkOffset = 0;
	public static int subTagChunkOffsets = 0;
	
	public static String[] isNotAppTag = new String[]{
			"uses-permission", "uses-sdk", "compatible-screens", "instrumentation", "library",
			"original-package", "package-verifier", "permission", "permission-group", "permission-tree",
			"protected-broadcast", "resource-overlay", "supports-input", "supports-screens", "upgrade-key-set",
			"uses-configuration", "uses-feature"};
	
	public static String prefixStr = "http://schemas.android.com/apk/res/android";

	public static void removeTag(String tagName, String name){
		ParserChunkUtils.parserXml();
		StartTagChunk start = null;
		for(Chunk chunk : ParserChunkUtils.xmlStruct.tagChunkList){
			if (chunk instanceof StartTagChunk) {
				start = (StartTagChunk) chunk;
			} else {
				continue;
			}
			String tagNameTmp = start.getName();
			if(tagName.equals(tagNameTmp)){
				for(AttributeData attrData : start.attrList){
					String attrName = attrData.getName();
					if("name".equals(attrName)){
						String value = attrData.getValueString();
						if(name.equals(value)){
							ParserChunkUtils.xmlStruct.tagChunkList.remove(start);
							modifyFile();
							return;
						}
					}
				}
			}
		}
	}

	public static void addTag(String insertXml){
		ParserChunkUtils.parserXml();
		try {
	        XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();  
	        XmlPullParser pullParser = pullParserFactory.newPullParser();  
	        pullParser.setInput(new FileInputStream(insertXml), "UTF-8");  
	        int event = pullParser.getEventType();
	        while (event != XmlPullParser.END_DOCUMENT){
	            switch (event) {
	                case XmlPullParser.START_DOCUMENT:
	                    break;
	                case XmlPullParser.START_TAG:
	                	String tagName = pullParser.getName();
	                	int name = getStrIndex(tagName);
	                	int attCount = pullParser.getAttributeCount();
	                	ArrayList<AttributeData> attrList = new ArrayList<>();
	                	for(int i=0;i<pullParser.getAttributeCount();i++){
	                		int attruri = getStrIndex(prefixStr);
	                		String attrName = pullParser.getAttributeName(i);
	                		String[] strAry = attrName.split(":");
	                		String tagValue = pullParser.getAttributeValue(i);
	                		int[] type = getAttrType(tagValue);
	                		int attrname = getStrIndex(strAry[1]);
	                		int attrvalue = getStrIndex(tagValue);
							if(tagValue.equals("true") || tagValue.equals("false")) {
								attrvalue = -1;
							}
	                		int attrtype = type[0];
	                		int attrdata = type[1];
	                		AttributeData data = AttributeData.createAttribute(attruri, attrname, attrvalue, attrtype, attrdata);
							attrList.add(data);
	                	}
	                	
	                	StartTagChunk startChunk = StartTagChunk.createChunk(name, attCount, -1, attrList);
	                	if(isNotAppTag(tagName)){
							ParserChunkUtils.xmlStruct.tagChunkList.add(ParserChunkUtils.sAppLicationTagIndex, startChunk);
	                	}else{
							ParserChunkUtils.xmlStruct.tagChunkList.add(ParserChunkUtils.sAppLicationTagIndex + 1, startChunk);
	                	}
	                    break;  
	                    
	                case XmlPullParser.END_TAG:
	                	tagName = pullParser.getName();
	                	name = getStrIndex(tagName);
	                	EndTagChunk endChunk = EndTagChunk.createChunk(name);
	                	if(isNotAppTag(tagName)){
							ParserChunkUtils.xmlStruct.tagChunkList.add(ParserChunkUtils.sAppLicationTagIndex, endChunk);
	                	}else{
							ParserChunkUtils.xmlStruct.tagChunkList.add(ParserChunkUtils.sAppLicationTagIndex + 2, endChunk);
	                	}
	                    break;  
	                    
	            }
	            event = pullParser.next();
	        }  
		} catch (XmlPullParserException e) {
			System.out.println("insert tag : " + e.getMessage());
		} catch (IOException e){
			System.out.println("insert tag : " + e.getMessage());
		}
		modifyAllChunk();
		modifyFile();
	}

	public static void removeAttr(String tag, String tagName, String attrName){
		ParserChunkUtils.parserXml();
		StartTagChunk start = null;
		for(Chunk chunk : ParserChunkUtils.xmlStruct.tagChunkList){
			if (chunk instanceof StartTagChunk) {
				start = (StartTagChunk) chunk;
			} else {
				continue;
			}
			String tagNameTmp = start.getName();
			if(start.equals(tagNameTmp)){
				for(AttributeData attrData : start.attrList){
					if("name".equals(attrData.getName())){
						if(tagName.equals(attrData.getValueString())){
							for(AttributeData data : start.attrList){
								if(attrName.equals(data.getName())){
									if (start.attrList.size() == 1) {
										ParserChunkUtils.xmlStruct.tagChunkList.remove(start);
									} else {
										start.removeAttribute(data);
									}
									modifyFile();
									return;
								}
							}
						}
					}
				}
			}
		}
	}

	public static void modifyAttr(String tag, String tagName, String attrName, String attrValue){
		ParserChunkUtils.parserXml();
		XmlEditor.removeAttr(tag, tagName, attrName);
		ParserChunkUtils.parserXml();
		XmlEditor.addAttr(tag, tagName, attrName, attrValue);
	}

	public static void addAttr(String tag, String tagName, String attrName, String attrValue){
		ParserChunkUtils.parserXml();
		int[] type = getAttrType(attrValue);
		int attrname = getStrIndex(attrName);
		int attrvalue = getStrIndex(attrValue);
		int attruri = getStrIndex(prefixStr);;
		int attrtype = type[0];
		int attrdata = type[1];
		AttributeData data = AttributeData.createAttribute(attruri, attrname, attrvalue, attrtype, attrdata);
		StartTagChunk start = null;
		for(Chunk chunk : ParserChunkUtils.xmlStruct.tagChunkList){
			if (chunk instanceof StartTagChunk) {
				start = (StartTagChunk) chunk;
			} else {
				continue;
			}
			String tagNameTmp = start.getName();
			if(tag.equals(tagNameTmp)){
				start.addAttribute(data);
				modifyAllChunk();
				modifyFile();
				return;
			}
		}
		
	}

	private static final int[] resIds = {16842755, 16842766, 16842768, 16842776};
	private static final String[] resNames = {"name", "enabled", "exported", "authorities"};
	private static void modifyAllChunk(){
		ResourceChunk resChunk = ParserChunkUtils.xmlStruct.resChunk;
		int ret = resChunk.insertProvider();
		if (ret > 0) {
			ArrayList<String> src = new ArrayList<>(ParserChunkUtils.xmlStruct.stringChunk.stringContentList);
			ArrayList<Integer> ids = ParserChunkUtils.xmlStruct.resChunk.resourcIdList;
			for (int i = 0; i < resIds.length; i++) {
				int to = ids.indexOf(resIds[i]);
				if (to > 0) {
					int from = src.indexOf(resNames[i]);
					if (to < from) {
						ParserChunkUtils.xmlStruct.stringChunk.stringContentList.add(to, resNames[i]);
					}
				}
			}
			ParserChunkUtils.updateXmlContent(src, ParserChunkUtils.xmlStruct.stringChunk.stringContentList);
		}
	}

	public static void modifyFile(){
		int offset = 0;
		byte[] tmp = new byte[ParserChunkUtils.xmlStruct.byteSrc.length * 2];
		System.arraycopy(ParserChunkUtils.xmlStruct.byteSrc, offset, tmp, offset, 4);
		offset += 8;
		byte[] stringChunk = ParserChunkUtils.xmlStruct.stringChunk.getByte();
		System.arraycopy(stringChunk, 0, tmp, offset, stringChunk.length);
		offset += stringChunk.length;
		byte[] resChunk = ParserChunkUtils.xmlStruct.resChunk.getByte();
		System.arraycopy(resChunk, 0, tmp, offset, resChunk.length);
		offset += resChunk.length;
		for (Chunk chunk : ParserChunkUtils.xmlStruct.tagChunkList) {
			byte[] chunkByte = chunk.getByte();
			System.arraycopy(chunkByte, 0, tmp, offset, chunkByte.length);
			offset += chunkByte.length;
		}
		ParserChunkUtils.xmlStruct.byteSrc = new byte[offset];
		System.arraycopy(tmp, 0, ParserChunkUtils.xmlStruct.byteSrc, 0, 4);
		System.arraycopy(Utils.int2Byte(offset), 0, ParserChunkUtils.xmlStruct.byteSrc, 4, 4);
		System.arraycopy(tmp, 8, ParserChunkUtils.xmlStruct.byteSrc, 8, offset - 8);
	}

	public static int getStrIndex(String str){
		if(str == null || str.length() == 0){
			return -1;
		}
		for(int i=0; i<ParserChunkUtils.xmlStruct.stringChunk.stringContentList.size(); i++){
			if(ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(i).equals(str)){
				return i;
			}
		}
		ParserChunkUtils.xmlStruct.stringChunk.stringContentList.add(str);
		return ParserChunkUtils.xmlStruct.stringChunk.stringContentList.size()-1;
	}

	public static boolean isNotAppTag(String tagName){
		for(String str : isNotAppTag){
			if(str.equals(tagName)){
				return true;
			}
		}
		return false;
	}

	public static int[] getAttrType(String tagValue){
		
		int[] result = new int[2];
		
		if(tagValue.equals("true") || tagValue.equals("false")){
			result[0] = result[0] | AttributeType.ATTR_BOOLEAN;
			if (tagValue.equals("false")) {
				result[1] = 0;
			} else {
				result[1] = -1;
			}
		}else if(tagValue.equals("singleTask") || tagValue.equals("standard") 
				|| tagValue.equals("singleTop") || tagValue.equals("singleInstance")){
			result[0] = result[0] | AttributeType.ATTR_FIRSTINT;
			if(tagValue.equals("standard")){
				result[1] = 0;
			}else if(tagValue.equals("singleTop")){
				result[1] = 1;
			}else if(tagValue.equals("singleTask")){
				result[1] = 2;
			}else{
				result[1] = 3;
			}
		}else if(tagValue.equals("minSdkVersion") || tagValue.equals("versionCode")){
			result[0] = result[0] | AttributeType.ATTR_FIRSTINT;
			result[1] = Integer.valueOf(tagValue);
		}else if(tagValue.startsWith("@")){
			result[0] = result[0] | AttributeType.ATTR_REFERENCE;
			result[1] = 0x7F000000;
		}else if(tagValue.startsWith("#")){
			result[0] = result[0] | AttributeType.ATTR_ARGB4;
			result[1] = 0xFFFFFFFF;
		}else{
			result[0] = result[0] | AttributeType.ATTR_STRING;
			result[1] = getStrIndex(tagValue);
		}
		
		result[0] = result[0] | 0x08000000;
		result[0] = Utils.byte2int(Utils.reverseBytes(Utils.int2Byte(result[0])));
		
		return result;
	}
	
}
