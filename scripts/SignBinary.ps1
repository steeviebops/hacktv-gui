param (
    [Parameter(Mandatory)]
    [string]$FileName
)

if ([string]::IsNullOrWhiteSpace($env:CERTUM_USERNAME)) {
    throw "No Certum username specified, aborting"
}

if ([string]::IsNullOrWhiteSpace($env:CERTUM_OTP_SECRET)) {
    throw "No OTP secret specified, aborting"
}

# TOTP generator
Add-Type -Language CSharp @"
using System;
using System.Security.Cryptography;

public static class Totp
{
    private const string B32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    private static byte[] Base32Decode(string s)
    {
        s = s.TrimEnd('=').ToUpperInvariant();

        int byteCount = s.Length * 5 / 8;
        byte[] bytes = new byte[byteCount];

        int buffer = 0;
        int bitsLeft = 0;
        int idx = 0;

        foreach (char c in s)
        {
            int val = B32.IndexOf(c);
            if (val < 0)
                throw new ArgumentException("Invalid Base32 char: " + c);

            buffer = (buffer << 5) | val;
            bitsLeft += 5;

            while (bitsLeft >= 8)
            {
                bitsLeft -= 8;
                bytes[idx++] = (byte)((buffer >> bitsLeft) & 0xFF);
            }

            // Keep only the unconsumed bits.
            if (bitsLeft > 0)
                buffer &= (1 << bitsLeft) - 1;
            else
                buffer = 0;
        }

        return bytes;
    }

    public static string Now(string secret, int digits, int period)
    {
        byte[] key = Base32Decode(secret);
        long counter = DateTimeOffset.UtcNow.ToUnixTimeSeconds() / period;

        byte[] cnt = BitConverter.GetBytes(counter);
        if (BitConverter.IsLittleEndian) Array.Reverse(cnt);

        byte[] hash = new HMACSHA256(key).ComputeHash(cnt);
        int offset = hash[hash.Length - 1] & 0x0F;
        int binary =
            ((hash[offset] & 0x7F) << 24) |
            ((hash[offset + 1] & 0xFF) << 16) |
            ((hash[offset + 2] & 0xFF) << 8) |
            (hash[offset + 3] & 0xFF);

        int otp = binary % (int)Math.Pow(10, digits);
        return otp.ToString(new string('0', digits));
    }
}
"@

function Get-TotpCode {
    param([string]$Secret,[int]$Digits=6,[int]$Period=30)
    [Totp]::Now($Secret,$Digits,$Period)
}
# End TOTP generator

try {
    # Get Certum TOTP code
    $otp = Get-TotpCode -Secret $env:CERTUM_OTP_SECRET -Digits 6 -Period 30
    # Run SimplySign Desktop with the OTP code we got
    & "C:\Program Files\Certum\SimplySign Desktop\SimplySignDesktop.exe" /autologin $env:CERTUM_USERNAME $otp
    Start-Sleep 10

    # Check if the signing cert has loaded succesfully
    $thumbprint = "D1011FE3EE42E77EE32411453DD5115D7727A1C1"
    if (-not (Get-ChildItem Cert:\CurrentUser\My |
            Where-Object Thumbprint -eq $thumbprint)) {
        throw "Signing certificate was not loaded by SimplySign Desktop."
    }
    Write-Host "Signing certificate loaded successfully."

    $signtool = Get-ChildItem `
        "C:\Program Files (x86)\Windows Kits\10\bin" `
        -Filter "signtool.exe" `
        -Recurse |
        Where-Object { $_.FullName -match "\\x64\\signtool\.exe$" } |
        Sort-Object FullName -Descending |
        Select-Object -First 1
    
    if (-not $signtool) {
        throw "Could not find signtool.exe"
    }
    Write-Host "Using SignTool: $($signtool.FullName)"
    & $signtool.FullName sign /sha1 $thumbprint /fd SHA256 /tr http://time.certum.pl/ /td SHA256 $FileName

    $exitCode = $LASTEXITCODE
    if ($exitCode -ne 0) {
        throw "signtool failed with exit code $exitCode"
    }
}
finally {
    & "C:\Program Files\Certum\SimplySign Desktop\SimplySignDesktop.exe" /close
}
