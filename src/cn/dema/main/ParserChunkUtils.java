package cn.dema.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.dema.chunk.EndNameSpaceChunk;
import cn.dema.chunk.EndTagChunk;
import cn.dema.chunk.ResourceChunk;
import cn.dema.chunk.StartNameSpaceChunk;
import cn.dema.chunk.StartTagChunk;
import cn.dema.chunk.StringChunk;
import cn.dema.chunk.Chunk;
import cn.dema.chunk.XmlStruct;

public class ParserChunkUtils {
	
	public static int stringChunkOffset = 8;
	public static int resourceChunkOffset;
	public static int nextChunkOffset;
	
	public static XmlStruct xmlStruct = new XmlStruct();
	
	public static boolean isApplication = false;
	public static boolean isManifest = false;
	
	public static void clear(){
		resourceChunkOffset = 0;
		nextChunkOffset = 0;
		isApplication = false;
		isManifest = false;
		xmlStruct.clear();
	}
	
	public static void parserXml(){
		clear();
		ParserChunkUtils.parserXmlHeader(xmlStruct.byteSrc);
		ParserChunkUtils.parserStringChunk(xmlStruct.byteSrc);
		ParserChunkUtils.parserResourceChunk(xmlStruct.byteSrc);
		ParserChunkUtils.parserXmlContent(xmlStruct.byteSrc);
	}

	public static void parserXmlHeader(byte[] byteSrc){
		byte[] xmlMagic = Utils.copyByte(byteSrc, 0, 4);
		byte[] xmlSize = Utils.copyByte(byteSrc, 4, 4);
		xmlStruct.magicNumber = xmlMagic;
		xmlStruct.fileSize = xmlSize;
	}

	public static void parserStringChunk(byte[] byteSrc){
		xmlStruct.stringChunk = StringChunk.createChunk(byteSrc, stringChunkOffset);
		byte[] chunkSizeByte = Utils.copyByte(byteSrc, 12, 4);
		resourceChunkOffset = stringChunkOffset + Utils.byte2int(chunkSizeByte);
		ArrayList<String> list = ParserChunkUtils.xmlStruct.stringChunk.stringContentList;
		for (int i = list.size() - 1; i >= 0; i --) {
			if (Arrays.toString(list.get(i).getBytes()).contains("-")) {
				System.out.println("remove " + list.remove(i));
			} else {
				break;
			}
		}
	}

	public static void parserResourceChunk(byte[] byteSrc){
		xmlStruct.resChunk = ResourceChunk.createChunk(byteSrc, resourceChunkOffset);
		byte[] chunkSizeByte = Utils.copyByte(byteSrc, resourceChunkOffset+4, 4);
		int chunkSize = Utils.byte2int(chunkSizeByte);
		nextChunkOffset = (resourceChunkOffset+chunkSize);
		XmlEditor.tagStartChunkOffset = nextChunkOffset;
	}

	public static void parserStartNamespaceChunk(byte[] byteSrc){
		xmlStruct.tagChunkList.add(StartNameSpaceChunk.createChunk(byteSrc));
	}

	public static void parserEndNamespaceChunk(byte[] byteSrc){
		xmlStruct.tagChunkList.add(EndNameSpaceChunk.createChunk(byteSrc));
	}
	
	public static int sAppLicationTagIndex = -1;

	public static void parserStartTagChunk(byte[] byteSrc, int offset){
		
		StartTagChunk tagChunk = StartTagChunk.createChunk(byteSrc, offset);
		xmlStruct.tagChunkList.add(tagChunk);

		byte[] tagNameByte = Utils.copyByte(byteSrc, 20, 4);
		int tagNameIndex = Utils.byte2int(tagNameByte);
		String tagName = xmlStruct.stringChunk.stringContentList.get(tagNameIndex);

		if (tagName.equals("application")){
			isApplication = true;
			sAppLicationTagIndex = xmlStruct.tagChunkList.size() - 1;
		}
	}

	public static void parserEndTagChunk(byte[] byteSrc, int offset){
		EndTagChunk tagChunk = EndTagChunk.createChunk(byteSrc, offset);
		xmlStruct.tagChunkList.add(tagChunk);
	}

	public static void parserXmlContent(byte[] byteSrc){
		while(!isEnd(byteSrc.length)){
			byte[] chunkTagByte = Utils.copyByte(byteSrc, nextChunkOffset, 4);
			byte[] chunkSizeByte = Utils.copyByte(byteSrc, nextChunkOffset+4, 4);
			int chunkTag = Utils.byte2int(chunkTagByte);
			int chunkSize = Utils.byte2int(chunkSizeByte);
			switch(chunkTag){
				case ChunkTypeNumber.CHUNK_STARTNS:
					parserStartNamespaceChunk(Utils.copyByte(byteSrc, nextChunkOffset, chunkSize));
					isManifest = true;
					break;
				case ChunkTypeNumber.CHUNK_STARTTAG:
					parserStartTagChunk(Utils.copyByte(byteSrc, nextChunkOffset, chunkSize), nextChunkOffset);
					if(isApplication){
						XmlEditor.subAppTagChunkOffset = nextChunkOffset+chunkSize;
						isApplication = false;
					}
					if(isManifest){
						XmlEditor.subTagChunkOffsets = nextChunkOffset+chunkSize;
						isManifest = false;
					}
					break;
				case ChunkTypeNumber.CHUNK_ENDTAG:
					parserEndTagChunk(Utils.copyByte(byteSrc, nextChunkOffset, chunkSize), nextChunkOffset);
					break;
				case ChunkTypeNumber.CHUNK_ENDNS:
					parserEndNamespaceChunk(Utils.copyByte(byteSrc, nextChunkOffset, chunkSize));
					break;
			}
			nextChunkOffset += chunkSize;
		}
		
	}

	public static void updateXmlContent(ArrayList<String> src, ArrayList<String> aim){
		for (Chunk chunk : xmlStruct.tagChunkList) {
			chunk.updateIndex(src, aim);
		}
	}

	public static boolean isEnd(int totalLen){
		return nextChunkOffset >= totalLen;
	}
	
}
