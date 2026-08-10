package com.vietsrepo.pricewatch.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.vietsrepo.pricewatch.dto.ErrorResponse;
import com.vietsrepo.pricewatch.enums.ErrorCode;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {
	
	private final JsonMapper mapper;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		String path = (String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
		if (path == null) {
			path = request.getRequestURI();
		}
		
		ErrorCode errorCode = (ErrorCode) request.getAttribute("JWT_ERROR_CODE");
		if (errorCode == null) {
			errorCode = ErrorCode.TOKEN_INVALID;
		}

		log.warn("Authentication failed: code={}, method={}, path={}, ip={}, requestId={}",
				errorCode.name(), request.getMethod(), path, request.getRemoteAddr(), request.getHeader("X-Request-ID"));

		ErrorResponse errorResponse = new ErrorResponse(
			errorCode.getStatus().getReasonPhrase(),
			errorCode.name(),
			errorCode.getMessage(),
			path,
			LocalDateTime.now()
		);

		ServletOutputStream out = response.getOutputStream();
		mapper.writeValue(out, errorResponse);
		out.flush();
	}
}
