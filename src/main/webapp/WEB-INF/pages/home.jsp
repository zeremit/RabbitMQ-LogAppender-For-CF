<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ page session="false" %>
<html>
  <head>
    <title>Simple RabbitMQ Application</title>
  </head>
  <body>
    <h1>Simple RabbitMQ Application</h1>

    <h2>Publish a message</h2>

    <form:form modelAttribute="message" action="/publish" method="post">
      <form:label for="value" path="value">Message to publish:</form:label>
      <form:input path="value" type="text"/>

      <input type="submit" value="Publish"/>
    </form:form>

    <c:if test="${published}">
      <p>Published a message!</p>
    </c:if>

    <h2>Get a message</h2>

    <form:form action="/get" method="post">
      <input type="submit" value="Get one"/>
    </form:form>

    <c:choose>
      <c:when test="${got_queue_empty}">
        <p><b>com.altoros.appender.RabbitMQAppender</b> Queue empty</p>
      </c:when>
      <c:when test="${got != null}">
        <p><b>com.altoros.appender.RabbitMQAppender</b> message: <c:out value="${got}"/></p>
      </c:when>
    </c:choose>
    <c:choose>
      <c:when test="${got_queue_empty_cf}">
        <p><b>com.altoros.appender.RabbitMQCFAppender</b> Queue empty</p>
      </c:when>
      <c:when test="${got_cf != null}">
        <p><b>com.altoros.appender.RabbitMQCFAppender</b> message: <c:out value="${got_cf}"/></p>
      </c:when>
    </c:choose>
  </body>
</html>
