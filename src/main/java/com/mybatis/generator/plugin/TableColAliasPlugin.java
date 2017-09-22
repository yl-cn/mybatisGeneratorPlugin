package com.mybatis.generator.plugin;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.JDBCConnectionConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;


public class TableColAliasPlugin extends PluginAdapter{



    String sql = "select CONCAT('${alias}','.',UPPER(COLUMN_NAME),' as ${prefix}',UPPER(COLUMN_NAME) , ',') as base_column FROM information_schema.`COLUMNS` where TABLE_NAME= ";



    @Override

    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {



        XmlElement sqlNode = new XmlElement("sql"); //$NON-NLS-1$

        context.getCommentGenerator().addComment(sqlNode);



        String tableName = introspectedTable.getFullyQualifiedTable().getIntrospectedTableName();



        StringBuilder sb = executeQuery(tableName);



        TextElement txElt = new TextElement(sb.toString());



        sqlNode.addElement(txElt);

        sqlNode.addAttribute(new Attribute("id","baseColAlias"));



        document.getRootElement().addElement(new TextElement("\n\n"));

        document.getRootElement().addElement(sqlNode);



        return true;

    }



    public StringBuilder executeQuery(String tableName){

        ResultSet rs = null;

        StringBuilder sb = new StringBuilder();

        try{

            Connection conn = getContextConnection(this.context);



            Statement stmt = conn.createStatement();

            rs = stmt.executeQuery(sql + "'" + tableName + "'");



            sb = new StringBuilder();

            while(rs.next()){

                String val = rs.getString(1);

                sb.append(val).append("\n");

            }

        }catch(Exception e){

            throw new RuntimeException(e);

        }

        //删除最后一个逗号

        if( sb.length() > 0  ){

            sb.deleteCharAt(  sb.lastIndexOf(",") );

        }

        return sb;

    }



    static Connection getContextConnection(Context context){

        Connection conn = null;

        try{

            JDBCConnectionConfiguration jdbcConfig = context.getJdbcConnectionConfiguration();

            String userId = jdbcConfig.getUserId();

            String driverClass = jdbcConfig.getDriverClass();

            String password = jdbcConfig.getPassword();

            String jdbcUrlStr = jdbcConfig.getConnectionURL();

            Class.forName(driverClass);

            conn = DriverManager.getConnection(jdbcUrlStr, userId, password);

        }catch(Exception e){

            e.printStackTrace();

        }

        return conn;

    }



    public boolean validate(List<String> warnings) {

        return true;

    }

}
