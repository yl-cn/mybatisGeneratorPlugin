package com.mybatis.generator.plugin;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.sql.Types;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSR303Plugin extends PluginAdapter {

    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field,
                                       TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                       IntrospectedTable introspectedTable, ModelClassType modelClassType) {

        String remarks = introspectedColumn.getRemarks();
        String fieldName = getFieldName(remarks, "\\s+");

        if (false == introspectedColumn.isNullable()){
            if (false == introspectedColumn.isIdentity()){
                if(true == introspectedColumn.isStringColumn() && haveExpression(remarks,"(\\s|)\\$NOWHITESPACE")) {
                    topLevelClass.addImportedType("org.hibernate.validator.constraints.NotBlank");
                    field.addAnnotation("@NotBlank(message = \"" + fieldName + "不能为空\")");
                }
                else {
                    topLevelClass.addImportedType("javax.validation.constraints.NotNull");
                    field.addAnnotation("@NotNull(message = \"" + fieldName + "不能为空\")");
                }
            }
        }

        if (true == introspectedColumn.isStringColumn()){

            if(haveExpression(remarks, "(\\s+||,|;)MAXLENGTH=[0-9]+") && StringUtils.isNotBlank(getValue(remarks, "MAXLENGTH=")) ){
                String value = getValue(remarks, "MAXLENGTH=");
                topLevelClass.addImportedType("javax.validation.constraints.Size");
                field.addAnnotation("@Size(max=" + value + " ,message = \"" + fieldName + "不能超过{max}字符\")");
            }
            else {
                topLevelClass.addImportedType("javax.validation.constraints.Size");
                //field.addAnnotation("@Size(min = 0, max = "+introspectedColumn.getLength()+" , message = \"" + fieldName + "长度必须在{min}和{max}之间\")");
                field.addAnnotation("@Size(max = "+introspectedColumn.getLength()+" , message = \"" + fieldName + "不能超过{max}字符\")");
            }

            if(introspectedColumn.getLength() > 20) {
                topLevelClass.addImportedType("org.hibernate.validator.constraints.SafeHtml");

                field.addAnnotation("@SafeHtml(message = \"" + fieldName + "包含不安全的内容\")");
            }

        }

        Map<String, String> constraints = parseRemarkToConstraint(fieldName, remarks, introspectedColumn.getJdbcType());

        if(MapUtils.isNotEmpty(constraints)) {

            for (Map.Entry<String, String> entry : constraints.entrySet()) {
                topLevelClass.addImportedType(entry.getKey());
                field.addAnnotation(entry.getValue());
            }
        }
/*        else {

            if (introspectedColumn.getJdbcType() == Types.INTEGER){
                topLevelClass.addImportedType("javax.validation.constraints.Max");
                field.addAnnotation("@Max(value=2147483647, message=\"" + fieldName + "最大值不能高于{value}\")");
                topLevelClass.addImportedType("javax.validation.constraints.Min");
                field.addAnnotation("@Min(value=-2147483648,message=\"" + fieldName + "最小值不能低于{value}\")");
            }

        }*/

        return super.modelFieldGenerated(field, topLevelClass, introspectedColumn,
                introspectedTable, modelClassType);
    }

    public Map<String, String> parseRemarkToConstraint(String fieldName, String remark, int fldType) {
        Map<String, String> constraints = new HashMap<>();

        switch (fldType) {
            case Types.TINYINT:
            case Types.BIGINT:
            case Types.INTEGER:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.NUMERIC:
                if(haveExpression(remark,"(\\s+||,|;)IN\\[[0-9,]+\\]")) {
                    String value = extractMessage(remark,'[',']').get(0);
                    if(StringUtils.isNotBlank(value)) {
                        String annotation = "@DataIn(in=\"" + value.trim() + "\",  message = \"" + fieldName + "数据不匹配\")";
                        constraints.put("com.gznb.member.validator.annotation.DataIn",annotation);
                    }

                }

                if(haveExpression(remark, "(\\s+||,|;)MAX=[0-9]+")) {
                    String value = getValue(remark, "MAX=");
                    if(StringUtils.isNotBlank(value)) {
                        constraints.put("javax.validation.constraints.Max","@Max(value=" + value + ", message = \"" + fieldName + "最大值为{value}\")");
                    }
                }

                if(haveExpression(remark, "(\\s+||,|;)MIN=[0-9]+")) {
                    String value = getValue(remark, "MIN=");
                    if(StringUtils.isNotBlank(value)) {
                        constraints.put("javax.validation.constraints.Min","@Min(value=" + value + ", message = \"" + fieldName + "最小值为{value}\" )");
                    }
                }
                break;
            case Types.DECIMAL:
                if(haveExpression(remark, "(\\s+||,|;)MAX=(0|[1-9]\\d{0,11})\\.(\\d\\d)+")) {
                    String value = getValue(remark, "MAX=");
                    if(StringUtils.isNotBlank(value)) {
                        constraints.put("javax.validation.constraints.DecimalMax","@DecimalMax(value=" + value + ", message = \"" + fieldName + "最大值为{value}\" )");
                    }
                }

                if(haveExpression(remark, "(\\s+||,|;)MIN=(0|[1-9]\\d{0,11})\\.(\\d\\d)+")) {
                    String value = getValue(remark, "MIN=");
                    if(StringUtils.isNotBlank(value)) {
                        constraints.put("javax.validation.constraints.DecimalMin","@DecimalMin(value=" + value + ", message = \"" + fieldName + "最小值为{value}\" )");
                    }
                }
                break;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGVARCHAR:
            case Types.LONGNVARCHAR:
                if(haveExpression(remark, "(\\s+||,|;)IN\\[[a-zA-Z0-9,]+\\]")) {
                    String in = extractMessage(remark,'[',']').get(0).replaceAll(",","|");
                    if(StringUtils.isNotBlank(in)) {
                        String annotation = "@Pattern(regexp = \"^\\\\s+||" + in + "$\", message = \"" + fieldName + "数据不匹配\")";
                        constraints.put("javax.validation.constraints.Pattern",annotation);
                    }
                }

                if(haveExpression(remark, "(\\s+||,|;)MINLENGTH=[0-9]+")) {
                    String value = getValue(remark, "MINLENGTH=");
                    if(StringUtils.isNotBlank(value)) {
                        constraints.put("org.hibernate.validator.constraints.Length","@Length(min=" + value + " ,message = \"" + fieldName + "不能少于{min}字符\")");
                    }
                }

                //日期格式字符串
                if(haveExpression(remark, "(\\s+||,|;)DF\\{[yYmMdDhHSsWw:\\s0-9\\-/]+}")) {
                    String value = extractMessage(remark,'{','}').get(0);
                    if(StringUtils.isNotBlank(value)) {
                        String annotation = "@DateFldFormat(datePattern=\"" + value.trim() + "\",  message = \"" + fieldName + "时间格式不匹配\")";
                        constraints.put("com.gznb.member.validator.annotation.DateFldFormat",annotation);
                    }
                }
                break;
        }

        return constraints;
    }

    public String getValue(String source, String def) {
        int index = source.indexOf(def);
        String value = "";

        if(index != -1) {
            String tmp = source.substring(index + def.length());
            String[] splitedStr = tmp.split("\\s|,|;");

            if(ArrayUtils.isNotEmpty(splitedStr)) {
                return splitedStr[0];
            }
        }

        return  value;
    }

    /**
     * 取中括号中间内容，忽略中括号里中括号
     * @param msg
     * @return
     */
    public List<String> extractMessage(String msg, char startChar, char endChar) {
        List<String> list = new ArrayList<>();
        int start = 0;
        int startFlag = 0;
        int endFlag = 0;
        for (int i = 0; i < msg.length(); i++) {
            if (msg.charAt(i) == startChar) {
                startFlag++;
                if (startFlag == endFlag + 1) {
                    start = i;
                }
            } else if (msg.charAt(i) == endChar) {
                endFlag++;
                if (endFlag == startFlag) {
                    list.add(msg.substring(start + 1, i));
                }
            }
        }
        return list;
    }

    public boolean haveExpression(String content, String reg){
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(content);
        while(m.find()){
            return true;
        }
        return false;
    }

    public String getFieldName(String remark, String delimiter) {
        if(StringUtils.isNotBlank(remark)) {
            String[] tmpArray = remark.split(StringUtils.isNotEmpty(delimiter) ? delimiter : "");
            if(ArrayUtils.isNotEmpty(tmpArray)) {
                return tmpArray[0];
            }
        }
        return "";
    }



}
