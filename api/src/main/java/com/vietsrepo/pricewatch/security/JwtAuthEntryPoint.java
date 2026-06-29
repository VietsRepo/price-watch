package com.vietsrepo.pricewatch.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
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
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper mapper;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		log.info("Vào filter rồi");
		String path = (String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
		if (path == null) {
			path = request.getRequestURI();
		}
		
		String method = request.getMethod();
		
		String ipAddress = request.getHeader("X-Forwarded-For");
		if (ipAddress != null && ipAddress.contains(",")) {
			ipAddress = ipAddress.split(",")[0].trim();
		}
		if (ipAddress == null || ipAddress.isBlank()) {
			ipAddress = request.getRemoteAddr();
		}
		
		String code = (String) request.getAttribute("JWT_ERROR_CODE");
		if (code == null) {
			code = ErrorCode.TOKEN_INVALID.name();
		}

		log.warn("Authentication failed: code={}, method={}, path={}, ip={}, requestId={}",
				code, method, path, ipAddress, request.getHeader("X-Request-ID"));

		ErrorResponse errorResponse = new ErrorResponse(
			HttpStatus.UNAUTHORIZED.getReasonPhrase(),
			code,
			ErrorCode.TOKEN_INVALID.getMessage(),
			path,
			LocalDateTime.now()
		);

		ServletOutputStream out = response.getOutputStream();
		mapper.writeValue(out, errorResponse);
		out.flush();
	}

}
