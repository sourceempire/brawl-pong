@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  brawl-pong-server startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and BRAWL_PONG_SERVER_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\brawl-pong-server-1.0-SNAPSHOT.jar;%APP_HOME%\lib\vertx-lang-kotlin-4.2.3.jar;%APP_HOME%\lib\kotlin-stdlib-jdk8-1.8.10.jar;%APP_HOME%\lib\vertx-sockjs-service-proxy-4.2.3.jar;%APP_HOME%\lib\vertx-web-4.2.3.jar;%APP_HOME%\lib\vertx-web-client-4.2.3.jar;%APP_HOME%\lib\vertx-web-common-4.2.3.jar;%APP_HOME%\lib\vertx-auth-jwt-4.2.3.jar;%APP_HOME%\lib\vertx-auth-common-4.2.3.jar;%APP_HOME%\lib\vertx-bridge-common-4.2.3.jar;%APP_HOME%\lib\vertx-service-proxy-4.2.3.jar;%APP_HOME%\lib\vertx-core-4.2.3.jar;%APP_HOME%\lib\jackson-core-2.13.1.jar;%APP_HOME%\lib\jackson-annotations-2.13.1.jar;%APP_HOME%\lib\jackson-module-kotlin-2.13.1.jar;%APP_HOME%\lib\jackson-databind-2.13.1.jar;%APP_HOME%\lib\kotlin-stdlib-jdk7-1.8.10.jar;%APP_HOME%\lib\kotlin-reflect-1.5.30.jar;%APP_HOME%\lib\kotlin-stdlib-1.8.10.jar;%APP_HOME%\lib\netty-handler-proxy-4.1.72.Final.jar;%APP_HOME%\lib\netty-codec-http2-4.1.72.Final.jar;%APP_HOME%\lib\netty-codec-http-4.1.72.Final.jar;%APP_HOME%\lib\netty-resolver-dns-4.1.72.Final.jar;%APP_HOME%\lib\netty-handler-4.1.72.Final.jar;%APP_HOME%\lib\netty-codec-socks-4.1.72.Final.jar;%APP_HOME%\lib\netty-codec-dns-4.1.72.Final.jar;%APP_HOME%\lib\netty-codec-4.1.72.Final.jar;%APP_HOME%\lib\netty-transport-4.1.72.Final.jar;%APP_HOME%\lib\netty-buffer-4.1.72.Final.jar;%APP_HOME%\lib\netty-resolver-4.1.72.Final.jar;%APP_HOME%\lib\netty-common-4.1.72.Final.jar;%APP_HOME%\lib\kotlin-stdlib-common-1.8.10.jar;%APP_HOME%\lib\annotations-13.0.jar;%APP_HOME%\lib\netty-tcnative-classes-2.0.46.Final.jar


@rem Execute brawl-pong-server
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %BRAWL_PONG_SERVER_OPTS%  -classpath "%CLASSPATH%" MainKt %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable BRAWL_PONG_SERVER_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%BRAWL_PONG_SERVER_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
