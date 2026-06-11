@ECHO OFF
setlocal enabledelayedexpansion
cd /d "%~dp0"
set count=0
echo Beschikbare Paper jars:
for %%f in (paper-*.jar) do (
    set /a count+=1
    echo !count!. %%f
    set "file!count!=%%f"
)
if %count%==0 ( echo Geen paper-*.jar gevonden & pause & exit /b )
set /p "choice=Kies nummer (1-%count%): "
set "jar=!file%choice%!"
echo Starten van %jar%...
java -Xms2G -Xmx3G -jar "%jar%" nogui
pause