package cn.qaiu.lz.web.http;

import cn.qaiu.lz.common.util.URLParamUtil;
import cn.qaiu.lz.web.service.CacheService;
import cn.qaiu.parser.PanDomainTemplate;
import cn.qaiu.vx.core.annotaions.RouteHandler;
import cn.qaiu.vx.core.annotaions.RouteMapping;
import cn.qaiu.vx.core.enums.RouteMethod;
import cn.qaiu.vx.core.util.AsyncServiceUtil;
import cn.qaiu.vx.core.util.ResponseUtil;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务API
 * <br>Create date 2021/4/28 9:15
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Slf4j
@RouteHandler("/")
public class ServerApi {

    private final CacheService cacheService = AsyncServiceUtil.getAsyncServiceInstance(CacheService.class);

    @RouteMapping(value = "/parser", method = RouteMethod.GET, order = 4)
    public Future<Void> parse(HttpServerResponse response, HttpServerRequest request, String pwd) {
        Promise<Void> promise = Promise.promise();
        String url = URLParamUtil.parserParams(request);

        PanDomainTemplate panDomainTemplate = PanDomainTemplate.fromShareUrl(url).setShareLinkInfoPwd(pwd);
        cacheService.getAndSaveCachedShareLink(panDomainTemplate)
                .onSuccess(res -> ResponseUtil.redirect(
                        response.putHeader("nfd-cache-hit", res.getCacheHit().toString())
                                .putHeader("nfd-cache-expires", res.getExpires()),
                                res.getDirectLink(), promise))
                .onFailure(t -> promise.fail(t.fillInStackTrace()));
        return promise.future();
    }

    @RouteMapping(value = "/json/parser", method = RouteMethod.GET, order = 3)
    public Future<String> parseJson(HttpServerRequest request, String pwd) {
        String url = URLParamUtil.parserParams(request);
        return PanDomainTemplate.fromShareUrl(url).setShareLinkInfoPwd(pwd).createTool().parse();
    }

    @RouteMapping(value = "/json/:type/:key", method = RouteMethod.GET, order = 2)
    public Future<String> parseKeyJson(String type, String key) {
        String code = "";
        if (key.contains("@")) {
            String[] keys = key.split("@");
            key = keys[0];
            code = keys[1];
        }
        return PanDomainTemplate.fromShortName(type)
                .generateShareLink(key).setShareLinkInfoPwd(code).createTool().parse();
    }

    @RouteMapping(value = "/:type/:key", method = RouteMethod.GET, order = 1)
    public Future<Void> parseKey(HttpServerResponse response, String type, String key) {
        Promise<Void> promise = Promise.promise();
        String code = "";
        if (key.contains("@")) {
            String[] keys = key.split("@");
            key = keys[0];
            code = keys[1];
        }

        PanDomainTemplate panDomainTemplate = PanDomainTemplate
                .fromShortName(type)
                .generateShareLink(key)
                .setShareLinkInfoPwd(code);

        cacheService.getAndSaveCachedShareLink(panDomainTemplate)
                .onSuccess(res -> ResponseUtil.redirect(
                        response.putHeader("nfd-cache-hit", res.getCacheHit().toString())
                                .putHeader("nfd-cache-expires", res.getExpires()),
                        res.getDirectLink(), promise))
                .onFailure(t -> promise.fail(t.fillInStackTrace()));
        return promise.future();
    }

}
