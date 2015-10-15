# JsonSerializationExtension
---
> :warning:本專案結構使用Netbeans建置

## 簡介
本專案對org.json套件提供序列化與反序列化支援，程式設計師可以引用本專案，並且使用註釋的方式簡單、快速的建構物件與JSON間的對應。
另外提供類型轉換器、屬性轉換器，可以客製化型別轉換方法，或者將本專案不支援的型別指定轉換方法，例如：byte[]轉換為JSON時，預設將輸出number[]，產生JSONArray，這個結果可能是程式設計師不需要的，您可以透過這個功能達成base64與byte[]間的互相轉換。在另一方面，在設計Android應用程式時，常使用JSON做為資料傳遞的格式，在沒有使用反序列化工具時，程式設計師必須一層層的取得屬性且不能輕鬆地看出資料型別，如果預先透過建置JSON物件模型，設計時可以快速了解JSON結構。

## 快速上手
### 1.在專案中引入類別庫
:warning:本範例以Netbeans為例，其餘IDE請自行查找引入方式。

1. 於專案列表中選擇要引入的專案，點選該項目按下滑鼠右鍵，清單中點選[Properties]

![img](https://raw.githubusercontent.com/XuPeiYao/JsonSerializationExtension/master/README_files/Import_Step1.PNG)

2. 於Project Properties視窗中Categories列表中選擇[Libraries]項目
3. 於詳細設定項目選擇[Compile]標籤下的[Add JAR/Folder]開啟檔案開啟視窗，選擇要引入的JAR檔

![img](https://raw.githubusercontent.com/XuPeiYao/JsonSerializationExtension/master/README_files/Import_Step2.PNG)

### 2.建構物件模型
1. 新增或開啟欲作為模型的類別
2. 針對Class加上可序列化聲明Annotations
```java
@JSONSerializable //聲明該類別可序列化
public class Student{//該類別必須要有預設建構子
    ...something...
}
```
3. 在欲允許序列化的欄位、方法加上JSON屬性聲名Annotations
```java
@JSONProperty//聲明該屬性可以被序列化輸出、輸入且屬性名稱輸出為欄位名稱
private String name;//private屬性也可以進行聲明

@JSONProperty(key="firstName",setable=false)//聲明該屬性僅可被序列化輸出，且重定義輸出屬性名稱為firstName
public String getFirstName(){
    ...something...
}

@JSONProperty(key="firstName",getable=false)//聲明該屬性僅可被序列化輸入，且重定義輸入來源屬性名稱為firstName
public void setFirstName(String firstName){
    ...something...
}
```
### 3.JSON與物件實體的轉換
1. 使用JSONConvert物件中的靜態方法serialize可以將物件序列化為JSON物件
```java
JSONObject json = JSONConvert.serialize(obj);//物件屬性也可為可序列化物件
```
2. 使用JSONConvert物件中的靜態方法deserialize可以將JSON物件轉換為物件實體
```java
Student obj = JSONConvert.deserialize(ChatData.class, json);//物件屬性也可為可序列化物件
```
