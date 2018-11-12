/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.web.servlet;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.RestApiExceptionMapper;
import org.bremersee.exception.model.RestApiException;
import org.bremersee.web.MediaTypeHelper;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.AbstractView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.springframework.web.servlet.view.xml.MappingJackson2XmlView;
import org.springframework.web.util.WebUtils;

/**
 * @author Christian Bremer
 */
@Validated
@Slf4j
public class ApiExceptionResolver implements HandlerExceptionResolver {

  private static final String MODEL_KEY = "error";

  private final RestApiExceptionMapper exceptionMapper;

  @Setter
  private Jackson2ObjectMapperBuilder objectMapperBuilder;

  public ApiExceptionResolver(
      final RestApiExceptionMapper exceptionMapper) {
    this.exceptionMapper = exceptionMapper;
  }

  @Override
  public ModelAndView resolveException(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final @Nullable Object handler,
      final Exception ex) {

    if (!isExceptionHandlerResponsible(request, handler)) {
      return null;
    }

    final RestApiException payload = exceptionMapper.build(ex, request.getRequestURI(), handler);

    ModelAndView modelAndView;
    final ResponseFormatAndContentType chooser = new ResponseFormatAndContentType(request);
    switch (chooser.getResponseFormat()) {
      case JSON:
        final MappingJackson2JsonView mjv = objectMapperBuilder == null
            ? new MappingJackson2JsonView()
            : new MappingJackson2JsonView(objectMapperBuilder.build());
        mjv.setContentType(chooser.getContentType());
        mjv.setPrettyPrint(true);
        mjv.setModelKey(MODEL_KEY);
        mjv.setExtractValueFromSingleKeyModel(true); // removes the MODEL_KEY from the output
        modelAndView = new ModelAndView(mjv, MODEL_KEY, payload);
        break;

      case XML:
        final MappingJackson2XmlView mxv = objectMapperBuilder == null
            ? new MappingJackson2XmlView()
            : new MappingJackson2XmlView(objectMapperBuilder.createXmlMapper(true).build());
        mxv.setContentType(chooser.getContentType());
        mxv.setPrettyPrint(true);
        mxv.setModelKey(MODEL_KEY);
        modelAndView = new ModelAndView(mxv, MODEL_KEY, payload);
        break;

      default:
        modelAndView = new ModelAndView(new EmptyView(payload));
    }

    if (StringUtils.hasText(chooser.getContentType())) {
      response.setContentType(chooser.getContentType());
    }
    final int statusCode = exceptionMapper.detectHttpStatus(ex, handler).value();
    applyStatusCodeIfPossible(request, response, statusCode);
    return modelAndView;
  }

  @SuppressWarnings("WeakerAccess")
  protected boolean isExceptionHandlerResponsible(
      final HttpServletRequest request,
      final @Nullable Object handler) {

    // TODO evaluate path
    if (handler == null) {
      return false;
    }
    final Class<?> cls = handler instanceof HandlerMethod
        ? ((HandlerMethod) handler).getBean().getClass()
        : handler.getClass();
    final boolean result = AnnotationUtils.findAnnotation(cls, RestController.class) != null;
    if (log.isDebugEnabled()) {
      log.debug("Is handler [" + handler + "] a rest controller? " + result);
    }
    return result;
  }

  @SuppressWarnings("WeakerAccess")
  protected final void applyStatusCodeIfPossible(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final int statusCode) {

    if (!WebUtils.isIncludeRequest(request)) {
      if (log.isDebugEnabled()) {
        log.debug("Applying HTTP status code " + statusCode);
      }
      response.setStatus(statusCode);
      request.setAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE, statusCode);
    }
  }

  enum ResponseFormat {
    JSON, XML, EMPTY
  }

  static class ResponseFormatAndContentType {

    @Getter(AccessLevel.PROTECTED)
    private ResponseFormat responseFormat;

    @Getter(AccessLevel.PROTECTED)
    private String contentType;

    ResponseFormatAndContentType(final @NotNull HttpServletRequest request) {
      final String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);
      if (MediaTypeHelper.canContentTypeBeJson(acceptHeader)) {
        responseFormat = ResponseFormat.JSON;
        contentType = MediaType.APPLICATION_JSON_VALUE;
      } else if (MediaTypeHelper.canContentTypeBeXml(acceptHeader)) {
        responseFormat = ResponseFormat.XML;
        contentType = MediaType.APPLICATION_XML_VALUE;
      } else {
        responseFormat = ResponseFormat.EMPTY;
        String type = acceptHeader;
        int i = type.charAt(',');
        if (i > 0) {
          type = type.substring(0, i);
        }
        i = type.charAt(';');
        if (i > 0) {
          type = type.substring(0, i);
        }
        contentType = type;
      }
    }
  }

  static class EmptyView extends AbstractView {

    final String errorMessage;

    final String errorCode;

    final String errorClassName;

    EmptyView(final @NotNull RestApiException payload) {
      this.errorMessage = StringUtils.hasText(payload.getMessage())
          ? payload.getMessage()
          : "No message available";
      this.errorCode = payload.getErrorCode();
      this.errorClassName = payload.getClassName();
    }

    @Override
    protected void renderMergedOutputModel(
        @Nullable final Map<String, Object> map,
        final HttpServletRequest httpServletRequest,
        final HttpServletResponse httpServletResponse) {

      if (StringUtils.hasText(errorMessage)) {
        httpServletResponse.addHeader(RestApiExceptionMapper.MESSAGE_HEADER_NAME, errorMessage);
      }
      if (StringUtils.hasText(errorCode)) {
        httpServletResponse.addHeader(RestApiExceptionMapper.CODE_HEADER_NAME, errorCode);
      }
      if (StringUtils.hasText(errorClassName)) {
        httpServletResponse.addHeader(RestApiExceptionMapper.CLASS_HEADER_NAME, errorClassName);
      }
    }

  }

}
