package com.mybatis.generator.plugin;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

/**
 * @author kangyonggan
 * @since 16/5/12
 */
public class LombokPlugin extends PluginAdapter {
    private FullyQualifiedJavaType dataAnnotation = new FullyQualifiedJavaType("lombok.Data");
    private FullyQualifiedJavaType builderAnnotation = new FullyQualifiedJavaType("lombok.Builder");
    private FullyQualifiedJavaType noArgsConstructorAnnotation = new FullyQualifiedJavaType("lombok.NoArgsConstructor");
    private FullyQualifiedJavaType allArgsConstructorAnnotation = new FullyQualifiedJavaType("lombok.AllArgsConstructor");

    public boolean validate(List<String> warnings) {
        return true;
    }

    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addDataAnnotation(topLevelClass);
        return true;
    }

    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addDataAnnotation(topLevelClass);
        return true;
    }

    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addDataAnnotation(topLevelClass);
        return true;
    }

    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return false;
    }

    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return false;
    }

    protected void addDataAnnotation(TopLevelClass topLevelClass) {
        topLevelClass.addImportedType(this.dataAnnotation);
        topLevelClass.addImportedType(this.builderAnnotation);
        topLevelClass.addImportedType(this.noArgsConstructorAnnotation);
        topLevelClass.addImportedType(this.allArgsConstructorAnnotation);

        topLevelClass.addAnnotation("@Data");
        topLevelClass.addAnnotation("@Builder");
        topLevelClass.addAnnotation("@NoArgsConstructor");
        topLevelClass.addAnnotation("@AllArgsConstructor");

    }
}
