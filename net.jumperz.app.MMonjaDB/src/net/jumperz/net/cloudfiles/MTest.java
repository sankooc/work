package net.jumperz.net.cloudfiles;

import net.jumperz.net.*;
import java.util.*;

public class MTest
{
public static String authUser = "kanatoko";
public static String authKey = "114fd901c9bca073486349a5b96305d6";
//--------------------------------------------------------------------------------
public static void main( String[] args )
throws Exception
{
testAuth();
testContainer1();
System.out.println( "OK" );
}
//--------------------------------------------------------------------------------
public static void p( Object o )
{
System.out.println( o );
}
//--------------------------------------------------------------------------------
private static void testContainer1()
throws Exception
{
MCloudFilesContext cfc = MCloudFilesContext.getInstance();
cfc.setAuthUser( authUser );
cfc.setAuthKey( authKey );

Map metaData = new HashMap();
metaData.put( "key1", "value1" );
cfc.createContainer( "test2", metaData );
cfc.setContainerCdnEnabled( "test2" );

MHttpResponse objectResponse = new MHttpResponse( "HTTP/1.0 200 OK\r\n\r\nhoge" );
cfc.uploadObject( "test2", "/obj2", objectResponse, metaData );
}
//--------------------------------------------------------------------------------
private static void testAuth()
throws Exception
{
MHttpRequest request = MCloudFilesUtil.getAuthRequest( authUser, authKey );
MHttpResponse response = MCloudFilesUtil.sendRequest( request );

try
	{
	if( response.getStatusCode() != 204 ){ throw new Exception(); }
	}
catch( Exception e )
	{
	p( response );
	throw e;
	}
}
//--------------------------------------------------------------------------------
}