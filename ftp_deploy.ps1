$ftpHost = "ftp://site93261.siteasp.net/wwwroot"
$user = "site93261"
$pass = "R!s7?9Co6b+D"
$localPath = "D:\Project 1\BTL\TRO24H\publish"

function Create-FtpDirectory {
    param([string]$targetUri)
    try {
        $req = [System.Net.FtpWebRequest]::Create($targetUri)
        $req.Credentials = New-Object System.Net.NetworkCredential($user, $pass)
        $req.Method = [System.Net.WebRequestMethods+Ftp]::MakeDirectory
        $req.UseBinary = $true
        $req.KeepAlive = $false
        $resp = $req.GetResponse()
        $resp.Close()
        Write-Host "Created directory: $targetUri"
    } catch {
        # Directory might already exist
    }
}

function Upload-FtpFile {
    param([string]$localFile, [string]$targetUri)
    try {
        $req = [System.Net.FtpWebRequest]::Create($targetUri)
        $req.Credentials = New-Object System.Net.NetworkCredential($user, $pass)
        $req.Method = [System.Net.WebRequestMethods+Ftp]::UploadFile
        $req.UseBinary = $true
        $req.KeepAlive = $false
        
        $content = [System.IO.File]::ReadAllBytes($localFile)
        $req.ContentLength = $content.Length
        $stream = $req.GetRequestStream()
        $stream.Write($content, 0, $content.Length)
        $stream.Close()
        
        $resp = $req.GetResponse()
        $resp.Close()
        Write-Host "Uploaded: $([System.IO.Path]::GetFileName($localFile)) ($($content.Length) bytes)"
    } catch {
        Write-Error "Failed to upload $localFile to $targetUri : $_"
    }
}

Write-Host "Starting deployment to $ftpHost..."

# Get all files and directories
$items = Get-ChildItem -Path $localPath -Recurse

# Create directories first
$dirs = $items | Where-Object { $_.PSIsContainer } | Sort-Object { $_.FullName.Length }
foreach ($d in $dirs) {
    $relative = $d.FullName.Substring($localPath.Length).Replace("\", "/")
    $targetDir = "$ftpHost$relative"
    Create-FtpDirectory -targetUri $targetDir
}

# Upload files
$files = $items | Where-Object { -not $_.PSIsContainer }
$total = $files.Count
$count = 0

foreach ($f in $files) {
    $count++
    if ($f.Name -eq "setup_cloud_db.sql") { continue }
    $relative = $f.FullName.Substring($localPath.Length).Replace("\", "/")
    $targetUri = "$ftpHost$relative"
    Write-Host "[$count/$total] Uploading: $relative"
    Upload-FtpFile -localFile $f.FullName -targetUri $targetUri
}

Write-Host "Deployment completed!"
