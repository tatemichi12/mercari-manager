package com.example.mercari.service;

import org.springframework.stereotype.Service;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProductDescriptionService {
    
    // Intel CPU generation patterns - must contain Intel/Core keyword or i[3579] prefix
    private static final Pattern INTEL_GENERATION_PATTERN = Pattern.compile(
        "(?i)(?:intel\\s+.*?|core\\s+.*?|^\\s*i[3579][-\\s])([1-9]\\d{3,4})[a-z]*"
    );
    
    // Alternative pattern for more complex Intel CPU names
    private static final Pattern INTEL_GENERATION_PATTERN_ALT = Pattern.compile(
        "(?i)(?:intel|core)\\s+.*?([1-9]\\d{3,4})[a-z]*"
    );
    
    /**
     * Intel CPUの世代番号を取得する
     * @param cpuName CPU名（例: "Intel Core i7-10750H", "i5-8250U", "Core i3-7100U"など）
     * @return 世代番号（例: 10, 8, 7など）、判定できない場合は0
     */
    public int getIntelGenerationNumber(String cpuName) {
        if (cpuName == null || cpuName.trim().isEmpty()) {
            return 0;
        }
        
        String cleanCpuName = cpuName.trim();
        
        // まず第1パターンで試す
        Matcher matcher = INTEL_GENERATION_PATTERN.matcher(cleanCpuName);
        if (matcher.find()) {
            String modelNumber = matcher.group(1);
            return extractGenerationFromModelNumber(modelNumber);
        }
        
        // 第2パターンで試す
        matcher = INTEL_GENERATION_PATTERN_ALT.matcher(cleanCpuName);
        if (matcher.find()) {
            String modelNumber = matcher.group(1);
            return extractGenerationFromModelNumber(modelNumber);
        }
        
        return 0; // 判定できない場合
    }
    
    /**
     * モデル番号から世代を抽出する
     * @param modelNumber モデル番号（例: "10750", "8250", "7100"）
     * @return 世代番号
     */
    private int extractGenerationFromModelNumber(String modelNumber) {
        if (modelNumber.length() >= 4) {
            // 4桁以上の場合、最初の1桁または2桁が世代
            if (modelNumber.startsWith("10") || modelNumber.startsWith("11") || 
                modelNumber.startsWith("12") || modelNumber.startsWith("13") ||
                modelNumber.startsWith("14") || modelNumber.startsWith("15")) {
                return Integer.parseInt(modelNumber.substring(0, 2));
            } else {
                // 8250, 7100 などの場合、最初の1桁が世代
                return Integer.parseInt(modelNumber.substring(0, 1));
            }
        } else if (modelNumber.length() >= 1) {
            // 3桁以下の場合、最初の1桁が世代
            return Integer.parseInt(modelNumber.substring(0, 1));
        }
        return 0;
    }
    
    /**
     * OSフィールドとCPU情報から適切なOSバージョンを決定する
     * @param osField OSフィールド（例: "Windows(GML)", "Windows", "その他"）
     * @param cpuName CPU名
     * @return 決定されたOSバージョン（例: "Windows 11 Pro", "Windows 10 Pro", "Windows 11 Home"）
     */
    public String determineOsVersion(String osField, String cpuName) {
        if (osField == null) {
            osField = "";
        }
        
        boolean isGml = osField.toLowerCase().contains("gml");
        int intelGeneration = getIntelGenerationNumber(cpuName);
        
        if (isGml) {
            // GMLの場合
            if (intelGeneration >= 8) {
                return "Windows 11 Pro";
            } else {
                return "Windows 10 Pro";
            }
        } else {
            // GML以外の場合
            if (intelGeneration >= 8) {
                return "Windows 11 Home";
            } else {
                return "Windows 10 Pro";
            }
        }
    }
    
    /**
     * 商品説明を構築する
     * @param baseDescription 基本の商品説明
     * @param osField OSフィールド
     * @param cpuName CPU名
     * @param acAdapterInfo ACアダプター情報（nullの場合は自動追記しない）
     * @return 構築された商品説明
     */
    public String buildDescription(String baseDescription, String osField, String cpuName, String acAdapterInfo) {
        StringBuilder description = new StringBuilder();
        
        // 基本説明を追加
        if (baseDescription != null && !baseDescription.trim().isEmpty()) {
            description.append(baseDescription.trim());
            if (!baseDescription.trim().endsWith("\n")) {
                description.append("\n");
            }
        }
        
        // OS情報を追加
        String osVersion = determineOsVersion(osField, cpuName);
        description.append("\n");
        description.append("【スペック】\n");
        description.append("OS: ").append(osVersion).append("\n");
        description.append("CPU: ").append(cpuName != null ? cpuName : "Intel CPU").append("\n");
        
        // インストール情報を追加
        description.append("\n");
        description.append("【インストール済みソフトウェア】\n");
        description.append(osVersion).append("インストール済み\n");
        
        // ACアダプター情報を自動追記（現行仕様維持）
        if (acAdapterInfo != null && !acAdapterInfo.trim().isEmpty()) {
            description.append("\n");
            description.append("【付属品】\n");
            description.append(acAdapterInfo.trim()).append("\n");
        }
        
        return description.toString();
    }
    
    /**
     * 既存の説明文からOS情報を更新する
     * @param existingDescription 既存の説明文
     * @param osField OSフィールド
     * @param cpuName CPU名
     * @return 更新された説明文
     */
    public String updateOsInDescription(String existingDescription, String osField, String cpuName) {
        if (existingDescription == null || existingDescription.trim().isEmpty()) {
            return buildDescription("", osField, cpuName, null);
        }
        
        String osVersion = determineOsVersion(osField, cpuName);
        
        // 既存のOS情報とインストール情報を新しいものに置換
        String updated = existingDescription
            .replaceAll("(?i)OS:\\s*Windows\\s+\\d+\\s+(?:Pro|Home)", "OS: " + osVersion)
            .replaceAll("(?i)Windows\\s+\\d+\\s+(?:Pro|Home)\\s*インストール済み", osVersion + "インストール済み");
        
        return updated;
    }
}