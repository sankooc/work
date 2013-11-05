package net.jumperz.app.MMonjaDBCore.test;

import net.jumperz.app.MMonjaDBCore.*;
import net.jumperz.app.MMonjaDBCore.action.*;
import net.jumperz.app.MMonjaDBCore.action.mj.*;
import net.jumperz.app.MMonjaDBCore.event.*;

import com.mongodb.*;
import net.jumperz.mongo.*;
import java.util.*;

import org.bson.types.*;

public class MDebug
{
//--------------------------------------------------------------------------------
public static void main( String[] args )
throws Exception
{


/*
System.out.println( action.parse( "mj connect ssh root@secure8-p1.scutum.jp /home/kanatoko/.ssh/id_dsa 127.0.0.1:27017" ) );
System.out.println( action.parse( "mj connect ssh secure8-p1.scutum.jp:60022 /root/.ssh/id_dsa 127.0.0.1:27017" ) );
/*
MongoOptions options = new MongoOptions();
options.threadsAllowedToBlockForConnectionMultiplier = 20;
options.connectionsPerHost = 2;

Mongo mongo = new Mongo( "192.168.3.205", options );
DB db = mongo.getDB( "test2" );
*/
//p( db.eval( "tojson(arguments[0])", new Object[]{ new CodeWScope( "function(){return 1}", new BasicDBObject( "()", "xxxxxxxx" ) ) } )); 
//db.getCollection( "test" ) .insert( new BasicDBObject( "a", new CodeWScope( "function(){return 1}", new BasicDBObject( "()", "xxxxxxxx" ) ) ), WriteConcern.SAFE );
//db.getCollection( "test" ) .insert( new BasicDBObject( "a", new Symbol( "a9aa" ) ), WriteConcern.SAFE );

//p( new BSONTimestamp( 0, 0) );
//db.getCollection( "test" ) .insert( new BasicDBObject( "a", new BSONTimestamp(0, 0) ), WriteConcern.SAFE );
//db.getCollection( "test" ) .insert( new BasicDBObject( "a", new MaxKey() ), WriteConcern.SAFE );

System.exit( 0 );
}
//--------------------------------------------------------------------------------
}