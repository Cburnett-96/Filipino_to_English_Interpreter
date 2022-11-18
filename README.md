# Filipino - Ingles Tagasalin [Filipino_to_English_Interpreter]
Isang android app na magbibigay-daan sa iyo upang isalin ang Filipino(Tagalog) sa Ingles gamit ang materyal na disenyo. [An android app which will allows you to translate Filipino(Tagalog) to English with material design.]

## ✔ Mga tampok na listahan. [Features List]:
- May Kakayahang mag-Offline kapag ginagamit. [Supports Offline Capability (Need internet connection at first launch)]
- Pagsasalin ng Teksto. [Text Translation (Filipino Text to English Text)]
- Kakayahan sa pag-input ng boses at maisalin sa teksto. [Supports voice input. [Speech to Text for Filipino language] (Internet Connection Needed)]
- Kakayahang kumuha ng imahe sa galerya at sa kamera upang maisalin sa teksto. [Support Select Text from Image/Camera (Filipino Text)]
- Kakayahan maisalin ang teksto sa boses. [Text to Speech (Filipino and English)]
- Kayang kopyahin ang tekstong nakalagay. [Copy Text (Filipino and English)]
- May Kakayahang iimbak ang mga tekstong na isalin. [Support History of Translated Text]
- May Kakayahang ipaborito ang mga tekstong na isalin. [Support Add to Favorites of Translated Text]
- At gamit na materyal na disenyo na galing sa google at may kakayahang baguhin ang tema sa malinaw o madilim. [It also has material design from google and capability of Light & Dark Mode theme.]

## ✔ Mga Pahintulot na ginamit. [Permission Used]:
- INTERNET (Kailangan para sa unang pagbukas ng aplikasyon at gamit din ito sa pagsalin ng boses sa teksto [internet connection at first launch and used of Speech to Text])
- RECORD_AUDIO (Gamit sa pag-input ng boses at maisalin sa teksto. [To used of Speech to Text])
- CAMERA & WRITE_EXTERNAL_STORAGE Gallery (Gamit sa pagkuha ng imahe sa galerya at sa kamera upang maisalin sa teksto. [To used of Text from Image/Camera])

## Android Screenshots
UI (Light/Dark Mode) | Speech to Text
:-------------------------:|:-------------------------:
<img src="https://github.com/Cburnett-96/Filipino_to_English_Interpreter/blob/master/Screenshoots/UI.gif?raw=true" alt="drawing" width="320"  /> | <img src="https://github.com/Cburnett-96/Filipino_to_English_Interpreter/blob/master/Screenshoots/STT.gif?raw=true" alt="drawing" width="320"  />
Text from Camera | Text from Gallery
<img src="https://github.com/Cburnett-96/Filipino_to_English_Interpreter/blob/master/Screenshoots/Camera.gif?raw=true" alt="drawing" width="320"  /> | <img src="https://github.com/Cburnett-96/Filipino_to_English_Interpreter/blob/master/Screenshoots/Gallery.gif?raw=true" alt="drawing" width="320"/>


### Gradle Used

Text from Image/Camera Google API
```groovy
implementation 'com.google.android.gms:play-services-mlkit-text-recognition:18.0.2'
```
Translator Google API
```groovy
implementation 'com.google.mlkit:translate:17.0.1'
```

### Getting Started
