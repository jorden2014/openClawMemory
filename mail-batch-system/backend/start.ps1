$env:JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"

$env:PYTHONIOENCODING="utf-8"

chcp 65001 | Out-Null

Write-Host "=========================================="
Write-Host "   邮件批量发送系统 - 启动脚本"
Write-Host "=========================================="
Write-Host ""

$javaPath = Get-Command java -ErrorAction SilentlyContinue
if (-not $javaPath) {
    Write-Host "错误: 未找到Java环境，请安装JDK 17+"
    exit 1
}

Write-Host "Java版本:"
java -version
Write-Host ""

$mvnPath = Get-Command mvn -ErrorAction SilentlyContinue
if ($mvnPath) {
    Write-Host "Maven版本:"
    mvn -version
    Write-Host ""
    
    Write-Host "开始编译项目..."
    mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Host "项目编译失败"
        exit 1
    }
    Write-Host "项目编译成功"
    Write-Host ""
}

Write-Host "启动应用..."
Write-Host "=========================================="

$jarFile = Get-ChildItem -Path "target" -Filter "mail-batch-system-*.jar" | Select-Object -First 1
if ($jarFile) {
    java -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -jar $jarFile.FullName
} else {
    Write-Host "未找到可执行的jar包，尝试使用Maven启动..."
    mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"
}

Write-Host ""
Write-Host "应用已停止"
Write-Host "=========================================="