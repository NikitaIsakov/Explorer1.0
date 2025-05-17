Готовый apk файл лежит в корне директории. Explorer.apk

## Инструкция для сборки apk-файла
- Клонирование репозитория
``` Shell
git clone https://github.com/NikitaIsakov/Explorer1.0.git  
```  

### Через Android Studio.
1. Открыть проект в Android Studio.
2. Сделать билд проекта.  ```Build -> Generate App Bundles or APKs -> Generate APKs```
3. Готовый `.apk` файл будет лежать
``` lua
app\build\outputs\apk\debug\app-debug.apk  
```  
### Через терминал:
1. Открытие директории
``` cmd
cd Explorer1.0  
```  
2. Установка переменных окружения (если не установлены в системе)
``` cmd
export JAVA_HOME=/путь/к/jdk  
export ANDROID_HOME=$HOME/Android/Sdk  
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools  
```  
3. Сборка
``` cmd
gradlew.bat assembleDebug  
```  
4. Готовый `.apk` файл будет лежать
``` lua
app\build\outputs\apk\debug\app-debug.apk  
```