package io.gitp.sbpick.pickgenerator.scraper.scrapebase

/**
 * 스크래핑할 페이지가 아직 소스사이트에 업로드 되어있지 않으면 해당 에러 던짐
 */
class RequiredPageNotFound(pageUrl: String) : Exception("required page '${pageUrl}' not found")