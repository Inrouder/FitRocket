"use strict";
(async function loadRelease(){
  const repo="https://github.com/Inrouder/FitRocket";
  const status=document.getElementById("release-status");
  const version=document.getElementById("release-version");
  const notes=document.getElementById("release-notes");
  const primary=document.getElementById("download-apk");
  const second=document.getElementById("download-secondary");
  const details=document.getElementById("read-release");
  const fallback=repo+"/releases";
  try{
    const request=await fetch("https://api.github.com/repos/Inrouder/FitRocket/releases/latest",
      {headers:{"Accept":"application/vnd.github+json"}});
    if(!request.ok)throw new Error("No public release");
    const release=await request.json();
    if(release.draft||release.prerelease)throw new Error("Not stable");
    const url=typeof release.html_url==="string"&&
      release.html_url.startsWith(repo+"/releases/tag/")?release.html_url:fallback;
    const assets=Array.isArray(release.assets)?release.assets:[];
    const asset=assets.find(item=>{
      const link=item.browser_download_url;
      return typeof link==="string"&&link.startsWith(repo+"/releases/download/")&&
        link.toLowerCase().endsWith(".apk");
    });
    const download=asset?asset.browser_download_url:url;
    primary.href=download;second.href=download;details.href=url;
    const tag=String(release.tag_name||"Aktuell");
    status.textContent="Neueste Version: "+tag+(asset?" · APK direkt verfügbar":" · Release ansehen");
    version.textContent="FitRocket "+tag;
    const body=typeof release.body==="string"?release.body.trim():"";
    notes.textContent=body?body.slice(0,1100):"Die aktuelle APK steht auf der Release-Seite bereit.";
  }catch(_error){
    status.textContent="Neueste Version über GitHub Releases abrufen";
    version.textContent="Releases ansehen";
    notes.textContent="Die neuesten Downloads und Versionshinweise findest du im GitHub-Repository.";
    primary.href=fallback;second.href=fallback;details.href=fallback;
  }
})();
