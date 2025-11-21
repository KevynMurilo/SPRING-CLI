# Security Policy

## Supported Versions

Currently supported versions with security updates:

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |

## Reporting a Vulnerability

If you discover a security vulnerability in Spring CLI, please report it responsibly.

### How to Report

**Please DO NOT create a public GitHub issue for security vulnerabilities.**

Instead, please report security issues by:

1. **Email**: Send details to [your-security-email@example.com]
2. **Subject**: Include "SECURITY" in the subject line
3. **Details**: Include:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Your suggested fix (if any)

### What to Expect

- **Acknowledgment**: Within 48 hours
- **Initial Assessment**: Within 1 week
- **Regular Updates**: Every 7 days until resolved
- **Disclosure**: Coordinated with you before public disclosure

## Security Best Practices

### For Users

When using Spring CLI-generated projects:

1. **Change Default Credentials**
   ```properties
   # NEVER use these in production
   spring.security.user.name=admin
   spring.security.user.password=admin123
   ```

2. **Generate New JWT Secrets**
   ```bash
   # Generate a secure secret
   openssl rand -base64 64
   ```

   Update in `application.properties`:
   ```properties
   jwt.secret=YOUR_GENERATED_SECRET_HERE
   ```

3. **Use Environment Variables**
   ```bash
   export DB_PASSWORD=secure_password
   export JWT_SECRET=your_secret
   ```

   In `application.properties`:
   ```properties
   spring.datasource.password=${DB_PASSWORD}
   jwt.secret=${JWT_SECRET}
   ```

4. **Enable HTTPS in Production**
   ```properties
   server.ssl.enabled=true
   server.ssl.key-store=classpath:keystore.p12
   server.ssl.key-store-password=${KEYSTORE_PASSWORD}
   ```

5. **Keep Dependencies Updated**
   ```bash
   mvn versions:display-dependency-updates
   ```

### For Contributors

When contributing to Spring CLI:

1. **Never Commit Secrets**
   - No API keys, passwords, or tokens
   - Use `.gitignore` properly
   - Review commits before pushing

2. **Validate User Input**
   ```python
   # Good
   if not artifact_id or len(artifact_id) == 0:
       raise ValueError("Invalid artifact ID")

   # Avoid
   artifact_id = input("Enter artifact ID: ")
   os.system(f"mkdir {artifact_id}")  # Command injection!
   ```

3. **Use Safe File Operations**
   ```python
   # Good
   with open(path, 'w') as f:
       f.write(content)

   # Avoid
   os.system(f"echo {content} > {path}")  # Shell injection!
   ```

4. **Sanitize Paths**
   ```python
   # Good
   path = Path(user_input).resolve()
   if not path.is_relative_to(base_dir):
       raise ValueError("Path traversal detected")
   ```

## Known Security Considerations

### Generated Projects

1. **Default Credentials**: All generated projects use default credentials. Users must change these before deployment.

2. **JWT Secret**: The default JWT secret is a placeholder. Users must generate a new secret.

3. **CORS**: No CORS configuration is included by default. Add if needed:
   ```java
   @Configuration
   public class CorsConfig {
       @Bean
       public WebMvcConfigurer corsConfigurer() {
           return new WebMvcConfigurer() {
               @Override
               public void addCorsMappings(CorsRegistry registry) {
                   registry.addMapping("/api/**")
                       .allowedOrigins("https://yourdomain.com")
                       .allowedMethods("GET", "POST", "PUT", "DELETE");
               }
           };
       }
   }
   ```

### CLI Tool

1. **Network Requests**: The CLI makes requests to Spring Initializr API. Ensure you trust your network.

2. **Cache File**: Metadata is cached locally. The cache file is world-readable on Unix systems.

3. **Temporary Files**: ZIP files are temporarily created and deleted. Ensure `/tmp` or equivalent is secure.

## Security Features

### Current

- Input validation for all user inputs
- Path traversal protection
- Safe file operations using Python's Path API
- No shell command execution with user input
- HTTPS for API requests
- Type hints for better code safety

### Planned

- [ ] Code signing for releases
- [ ] Checksum verification for downloads
- [ ] SBOM (Software Bill of Materials) generation
- [ ] Automated security scanning in CI/CD
- [ ] Dependency vulnerability scanning

## Dependencies Security

Spring CLI uses these dependencies:

```
requests>=2.31.0
InquirerPy>=0.3.4
rich>=13.7.0
jinja2>=3.1.2
```

We monitor these for security vulnerabilities and update promptly.

To check for vulnerabilities:

```bash
pip install safety
safety check -r requirements.txt
```

## Disclosure Policy

When a security vulnerability is reported and fixed:

1. **Private Fix**: We develop a fix privately
2. **Security Advisory**: We publish a GitHub Security Advisory
3. **Patch Release**: We release a patched version
4. **Public Disclosure**: We publicly disclose details after patch is available
5. **Credit**: We credit the reporter (unless they prefer anonymity)

Typical timeline: 2-4 weeks from report to public disclosure.

## Security Checklist for Production

Before deploying a Spring CLI-generated project to production:

- [ ] Changed all default passwords
- [ ] Generated new JWT secret
- [ ] Configured HTTPS/TLS
- [ ] Set up proper CORS policies
- [ ] Configured database credentials via environment variables
- [ ] Enabled security headers
- [ ] Set up rate limiting
- [ ] Configured proper logging (no sensitive data)
- [ ] Updated all dependencies
- [ ] Configured firewall rules
- [ ] Set up monitoring and alerting
- [ ] Performed security testing
- [ ] Reviewed application.properties for sensitive data

## Resources

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [NIST Security Guidelines](https://www.nist.gov/cyberframework)
- [CWE Top 25](https://cwe.mitre.org/top25/)

## Contact

For security concerns: [your-security-email@example.com]

For general questions: [GitHub Issues](https://github.com/yourusername/spring-cli/issues)

---

**Security is a shared responsibility. Thank you for helping keep Spring CLI and its users safe!**
