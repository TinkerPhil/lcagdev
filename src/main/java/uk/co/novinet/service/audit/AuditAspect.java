package uk.co.novinet.service.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uk.co.novinet.auth.MyBbUserPrincipal;

import static java.time.Instant.now;
import static java.util.Arrays.asList;

@Aspect
@Component
public class AuditAspect {

    @Autowired
    private AuditDao auditDao;

    @Around("@annotation(uk.co.novinet.service.audit.Audit)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof MyBbUserPrincipal) {
            Signature signature = joinPoint.getSignature();

            AuditEvent auditEvent = new AuditEvent(
                    null,
                    now(),
                    ((MyBbUserPrincipal) principal).getMember().getId(),
                    signature.getDeclaringType().getName() + "::" + signature.getName(),
                    asList(joinPoint.getArgs()).toString()
            );

            auditDao.create(auditEvent);
        }

        return joinPoint.proceed();
    }

}