# ProductDescriptionService Documentation

## Overview

The `ProductDescriptionService` provides automatic OS version determination and product description building based on Intel CPU generation detection and OS field values, specifically handling GML (Global Market License) requirements.

## Key Features

### 1. Intel CPU Generation Detection

The service can automatically detect Intel CPU generations from CPU names using intelligent pattern matching:

```java
ProductDescriptionService service = new ProductDescriptionService();

int generation = service.getIntelGenerationNumber("Intel Core i7-10750H"); // Returns 10
int generation = service.getIntelGenerationNumber("Intel Core i5-8250U");  // Returns 8
int generation = service.getIntelGenerationNumber("AMD Ryzen 5 3600");      // Returns 0 (not Intel)
```

### 2. Automatic OS Version Determination

Based on the OS field and CPU generation, the service automatically determines the appropriate Windows version:

| OS Field Contains "GML" | Intel Generation | Result OS Version |
|-------------------------|------------------|-------------------|
| ✅ Yes | 8th gen or higher | Windows 11 Pro |
| ✅ Yes | 7th gen or lower | Windows 10 Pro |
| ❌ No | 8th gen or higher | Windows 11 Home |
| ❌ No | 7th gen or lower | Windows 10 Pro |

```java
// GML examples
String os1 = service.determineOsVersion("Windows(GML)", "Intel Core i7-10750H"); // "Windows 11 Pro"
String os2 = service.determineOsVersion("Windows(GML)", "Intel Core i5-6200U");  // "Windows 10 Pro"

// Non-GML examples  
String os3 = service.determineOsVersion("Windows", "Intel Core i7-10750H"); // "Windows 11 Home"
String os4 = service.determineOsVersion("Windows", "Intel Core i5-6200U");  // "Windows 10 Pro"
```

### 3. Product Description Building

The service can build complete product descriptions with automatic OS information, installation messages, and AC adapter information:

```java
String description = service.buildDescription(
    "高性能ゲーミングノートPC。軽量でポータブル。",  // Base description
    "Windows(GML)",                                    // OS field
    "Intel Core i7-10750H",                          // CPU name
    "ACアダプター、マウス付属"                         // AC adapter info (optional)
);
```

**Output:**
```
高性能ゲーミングノートPC。軽量でポータブル。

【スペック】
OS: Windows 11 Pro
CPU: Intel Core i7-10750H

【インストール済みソフトウェア】
Windows 11 Proインストール済み

【付属品】
ACアダプター、マウス付属
```

## Supported CPU Formats

The service recognizes various Intel CPU naming formats:

- `Intel Core i7-10750H`
- `Intel Core i5-8250U` 
- `Core i3-7100U`
- `i7-10750H`
- `i5-8250U`

## Generation Detection Rules

- **10th+ generation**: `10xxx`, `11xxx`, `12xxx`, `13xxx`, `14xxx`, `15xxx` → Extract first 2 digits
- **1st-9th generation**: `1xxx`-`9xxx` → Extract first 1 digit
- **Non-Intel CPUs**: Return 0 (generation unknown)

## Integration Examples

### With Controllers
```java
@Autowired
private ProductDescriptionService productDescriptionService;

@PostMapping("/items")  
public String createItem(@ModelAttribute Item item) {
    String description = productDescriptionService.buildDescription(
        item.getBaseDescription(),
        item.getOsField(), 
        item.getCpuName(),
        item.getAcAdapterInfo()
    );
    item.setDescription(description);
    // Save item...
}
```

## Testing

Run tests with:
```bash
./mvnw test -Dtest=ProductDescriptionServiceTest
```