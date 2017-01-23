<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!--************************************************************************************** 
	메세지박스 - Information : Start
*****************************************************************************************-->
<!-- layer_popup_form type_popup : 시작 -->
<div id="sys_msgBox" class="pop_wrap">
	<div class="popup_form message pop_info">
		<div name="popup_handleBar" class="h_handle drag_handle">
		</div>
		<button class="btn_popup_close" type="button">
			<span class="blind">Close</span>
		</button>
		<div class="txt_box">
			<p class="txt" name="txt_message" style="padding-bottom: 20px">
				<span class="hlight">메세지를 출력합니다.</span><br />메세지를 이곳에 출력합니다.
			</p>
		</div>
		<div class="btn_pop_message">
			<a class="btn_pop_common" href="#" name='btn_msgBox_ok'><span>OK</span></a>
		</div>

	</div>
	<div class="popup_bg"></div>
</div>
<!-- layer_popup_form type_popup : 끝 -->

<!-- layer_popup_form type_popup : 시작 -->
<div id="sys_resultBox" class="pop_wrap">
	<div class="popup_form message pop_result">
		<div name="popup_handleBar" class="h_handle drag_handle">
		</div>
		<button class="btn_popup_close" type="button">
			<span class="blind">Close</span>
		</button>
		<div class="txt_box">
			<p class="txt" name="txt_message">
				<span class="hlight">메세지를 출력합니다.</span><br />메세지를 이곳에 출력합니다.
			</p>
		</div>
		<div class="button">
			<a id="btn_popup_close" class="btn_popup_close" href="#"><span>Close</span></a>
		</div>
	</div>
	<div class="popup_bg"></div>
</div>
<!-- layer_popup_form type_popup : 끝 -->

<!-- layer_popup_form type_popup : 시작 -->
<div id="sys_msgCustmBox" class="pop_wrap">
	<div class="popup_form message pop_info">
		<div name="popup_handleBar" class="h_handle drag_handle">
		</div>
		<button class="btn_popup_close" type="button">
			<span class="blind">Close</span>
		</button>
		<div class="txt_box">

		</div>
		<div class="btn_pop_message">
			<a class="btn_pop_common" href="#" name='btn_msgBox_ok'><span>OK</span></a>
		</div>

	</div>
	<div class="popup_bg"></div>
</div>
<!-- layer_popup_form type_popup : 끝 -->

<script type="text/javascript">
<%-- ==================================================================Start====
  * cfn_msgBox 시스템 메세지 박스 출력 [!!!수정금지!!!]
  * 작성자		: 김광일
  * 작성일		: 2013.05
  * 
  * [options filed]
  * type			: Message type [error / warning / info]
  * title				: Title text
  * message	: Message text
  * traceInfo	: Trace Information text
  * OK				: 확인(OK) 버튼 클릭시, 처리할 이벤트
  ========================================================================== --%>
function cfn_msgBox(options){
	  $.extend(options, {type: "info", objId:"sys_msgBox"});
	  
	  <%-- 메세지박스 표시. --%>
	  showMessageBox(options);
	  $("#"+options.objId+" a[name=btn_msgBox_ok]").focus();
	  
	  if(typeof devYN != "undefined" && devYN  == "Y"){
		(function(){
			LANG_MANAGER.initialize({
				"gubun" : "message",
				"tagId" : "sys_msgBox" 
			});
		})();
	  }

	  /* 버튼 이벤트 등록. */
	  $("#"+options.objId+" a[name=btn_msgBox_ok]").off('click').trigger("focus").addClass('focus').on('click', function(){
  		  hideMessageBox($(this));
		  if (options.OK&& typeof(options.OK) == "function") { options.OK(); }
		
	  });

  	if(typeof options["beforeloading"] == "function"){
	  	options["beforeloading"](options);
  	}

	if(options["customEvent"]){
		if(options["customEvent"]["setEvent"]){
			$('#' + options["customEvent"]["eventId"]).on(options["customEvent"]["event"],function(){

				if(options["customEvent"]["isCloseMsgPop"]){
					 hideMessageBox($("#"+options.objId+" a[name=btn_msgBox_ok]"));
				}
				
				options["customEvent"]["eventFunc"](options)
			});
	 	}
	}
}

<%-- ==================================================================Start====
  * cfn_msgCustmBox 사용자 메시지 박스 출력 
  * 
  * [options filed]
  * type			: Message type [error / warning / info]
  * title				: Title text
  * message	: Message text
  * traceInfo	: Trace Information text
  * OK				: 확인(OK) 버튼 클릭시, 처리할 이벤트
  ========================================================================== --%>
function cfn_msgCustmBox(options){ 
	  $.extend(options, {type: "info", objId:"sys_msgCustmBox"});
	  
	  <%-- 메세지박스 표시. --%>
	  showCustomMessageBox(options);
	  $("#"+options.objId+" a[name=btn_msgBox_ok]").focus();
	  
	  if(typeof devYN != "undefined" && devYN  == "Y"){
		(function(){
			LANG_MANAGER.initialize({
				"gubun" : "message",
				"tagId" : "sys_msgCustmBox" 
			});
		})();
	  }

	  /* 버튼 이벤트 등록. */
	  $("#"+options.objId+" a[name=btn_msgBox_ok]").off('click').trigger("focus").addClass('focus').on('click', function(){
		  hideCustomMessageBox($(this));
		  if (options.OK&& typeof(options.OK) == "function") { options.OK(); }
		
	  });
}
/*==================================================================End==== */
</script>

<script type="text/javascript">
 /*==================================================================Start====
  * cfn_resultBox 시스템 메세지 박스 출력 [!!!수정금지!!!]
  * 작성자		: 김광일
  * 작성일		: 2013.05
  * 
  * [options filed]
  * type			: Message type [error / warning / info]
  * title				: Title text
  * message	: Message text
  * traceInfo	: Trace Information text
  * OK				: 확인(OK) 버튼 클릭시, 처리할 이벤트
  ==========================================================================*/
function cfn_resultBox(options){
	  $.extend(options, {type: "info", objId:"sys_resultBox"});
	  
	  // 메세지박스 표시.
	  showMessageBox(options);
	  $("#"+options.objId+" a[name=btn_msgBox_ok]").focus();
	  
	  /* 버튼 이벤트 등록. */
	  $("#"+options.objId+" a[name=btn_msgBox_ok]").off('click').trigger("focus").on('click', function(){
  		  hideMessageBox($(this));
		  if (options.OK&& typeof(options.OK) == "function") { options.OK(); }
		
	  });
}
  /*==================================================================End==== */
</script>
<!--************************************************************************************** 
	메세지박스 - Information : End
*****************************************************************************************-->

<!--************************************************************************************** 
	오류창(ErrorBox) : Start
*****************************************************************************************-->
<!-- layer_popup_form type_popup : 시작 -->
<div id="sys_errorBox" class="pop_wrap">
	<div class="pop_error popup_form">
		<div id="handle_bar" class="h_handle drag_handle"></div>
		<!--button class="btn_popup_close" type="button">
			<span class="blind">Close</span>
		</button-->
	    <div class="pop_header_btn">
	    	<ul>
	        	<li><a class="details" href="#" id="openDetail2">details</a></li>
	            <li><a class="close" href="#" id="popup_close" name="btn_msgBox_ok">close</a></li>
	        </ul>
	    </div>		
		<div class="txt_box">
			<p class="info_words"> 
				<span class="hlight">&nbsp;</span><br/>Please contact your administrator. 
			</p>
			<div id="errorDetail" style="display:none">
				<div class="error_message" style="height:70px;"> 
				  <p name="txt_message">시스템 오류 메세지 출력 화면 입니다.
		          이곳에 오류 내용이 나옵니다. 행간 및 좌우 상하 여백 유지해 주시기 바랍니다.
		          시스템 오류 메세지 출력 화면 입니다.
		          이곳에 오류 내용이 나옵니다. 행간 및 좌우 상하 여백 유지해 주시기 바랍니다.
		          시스템 오류 메세지 출력 화면 입니다.
		          이곳에 오류 내용이 나옵니다. 행간 및 좌우 상하 여백 유지해 주시기 바랍니다. 
		          </p>
		        </div>
				<div class="error_stack" style="height:140px;"> <p name="txt_msgBox_error_trace">
				   
		          </p>
	        	</div>
				<div class="opinion">
		          <h4><span class="ico"></span>Opinion</h4>
		          <div class="opinion_box">
		            <textarea class="opinion_input" style="height:80px;" name="" cols="" rows=""></textarea>
		          </div>
		        </div>
			</div>
		</div>
		
		 <div class="btn_pop_message"><a id="openDetail" class="btn_common" href="#" style="width:40px"><span>Details</span></a> 
		 <a id="sendOpinion" class="btn_common" href="#" style="width:40px"><span>Send</span></a> 
		 <a id="popup_close" name="btn_msgBox_ok" class="btn_common" href="#" style="width:40px"><span>Close</span></a> </div>
	</div>
	<div class="popup_bg"></div>
</div>
<!-- layer_popup_form type_popup : 끝 -->

<script type="text/javascript">
//	$("#sys_errorBox>.popup_form").resizable({
//	maxWidth : 570,
//	minWidth : 570
//});

$(".btn_pop_function_set>.btn_box>.btn_common[name=btn_msgBox_details]").on("click", function(){
	$("#sys_errorBox div.popup_form div.error_trace").toggle("blind", "fast"	);
});
 /*==================================================================Start====
  * cfn_errorBox 시스템 메세지 박스 출력 [!!!수정금지!!!]
  * 작성자		: 김광일
  * 작성일		: 2013.05
  * 
  * [options filed]
  * code			: Error Code
  * title				: Title text
  * message	: Message text
  * traceInfo	: Trace Information text
  * beforeOK	: 확인(OK) 버튼 클릭시, OK 이벤트보다 먼저 처리할 이벤트
  * OK				: 확인(OK) 버튼 클릭시, 처리할 이벤트
  ==========================================================================*/
$(document).ready(function() {
	$('#sendOpinion').hide();
	$('#openDetail, #openDetail2').click(function() {
		$('#errorDetail').toggle(300, 
			function () {
			$('#sendOpinion').toggle();
		});
		return false;
	});
});

function cfn_errorBox(options){
	  
	  $.extend(options, {type: "error", objId: "sys_errorBox"});
	  
	  // 메세지박스 표시.
	  showMessageBox(options);
	  
	  if(typeof devYN != "undefined" && devYN  == "Y"){
		(function(){
			LANG_MANAGER.initialize({
				"gubun" : "message",
				"tagId" : "sys_errorBox" 
			});
		})();
	  }
		
	  /* 버튼 이벤트 등록. */
	  $("#"+options.objId+" a[name=btn_msgBox_ok]").off('click').trigger("focus").on('click', function(){
		  if($('#errorDetail').css('display') != 'none'){
			  $('#errorDetail').toggle(300, 
				function () {
				$('#sendOpinion').toggle();
			  });
		  }
		  // 추후 DB 처리 여기에 추가.
		  
		  // OK이벤트를 클릭시 OK이벤트 처리 전에 수행할 함수 처리.
		  if (options.beforeOK&& typeof(options.beforeOK) === "function") {
			  if(!options.beforeOK()) {
				  return false;
			  }
		  }
		  // OK 이벤트 처리.
		  hideMessageBox($(this));
		  if (options.OK&& typeof(options.OK) == "function") { options.OK(options.code, options.message); }
	  });
}
  /*==================================================================End==== */
</script>
<!--************************************************************************************** 
	오류창(ErrorBox) : End
*****************************************************************************************-->

<!--************************************************************************************** 
	메세지박스 - Interactive : Start
*****************************************************************************************-->
<!-- layer_popup_form type_popup : 시작 -->
<div id="sys_confirmBox" class="pop_wrap">
	<div class="popup_form pop_confirm">
		<div id="handle_bar" class="h_handle"></div>
		<div class="txt_box"  style="margin-top: 50px">
			<p name="txt_message" style="padding: 50px">
				<span class="hlight_g">선택한 자료를 삭제 하시겠습니까?</span><br /> 세로방향 가운데정열로 4줄 예상한 예시글입니다.
			</p>
		</div>
		<div class="btn_pop_message">
	    	<a class="btn_pop_common" href="#" id="btn_confirm_ok">OK</span></a>
			<a class="btn_pop_common" href="#" id="btn_confirm_yes"><span>Yes</span></a>
			<a class="btn_pop_common" href="#" id="btn_confirm_no"><span>No</span></a>
			<a class="btn_pop_common" href="#" id="btn_confirm_cancel"><span>Cancel</span></a>
	    </div>
		<!-- <div class="btn_pop_function_set">
			<div class="btn_box">
				<a class="btn_common" href="#" id="btn_confirm_ok"><span>OK</span></a>
				<a class="btn_common" href="#" id="btn_confirm_yes"><span>Yes</span></a>
				<a class="btn_common" href="#" id="btn_confirm_no"><span>No</span></a>
				<a class="btn_common" href="#" id="btn_confirm_cancel"><span>Cancel</span></a>
			</div>
		</div> -->
	</div>
	<div class="popup_bg"></div>
</div>
<!-- layer_popup_form type_popup : 끝 -->


<!-- layer_popup_form type_popup : 시작 -->
<div id="sys_confirmBox2" class="pop_wrap">
	<div class="pop_confirm2 popup_form">
		<div id="handle_bar" class="h_handle"></div>
		<div class="txt_box"  style="margin-top: 50px">
			<p name="txt_message" style="padding: 50px">
				<span class="hlight_g">선택한 자료를 삭제 하시겠습니까?</span><br /> 세로방향 가운데정열로 4줄 예상한 예시글입니다.
			</p>
		</div>
		<div class="btn_pop_message">
	    	<a class="btn_pop_common" href="#" id="btn_confirm_ok2"><span>OK</span></a>
			<a class="btn_pop_common" href="#" id="btn_confirm_yes2"><span>Yes</span></a>
			<a class="btn_pop_common" href="#" id="btn_confirm_no2"><span>No</span></a>
			<a class="btn_pop_common" href="#" id="btn_confirm_cancel2"><span>Cancel</span></a>
	    </div>
		<!-- <div class="btn_pop_function_set">
			<div class="btn_box">
				<a class="btn_common" href="#" id="btn_confirm_ok"><span>OK</span></a>
				<a class="btn_common" href="#" id="btn_confirm_yes"><span>Yes</span></a>
				<a class="btn_common" href="#" id="btn_confirm_no"><span>No</span></a>
				<a class="btn_common" href="#" id="btn_confirm_cancel"><span>Cancel</span></a>
			</div>
		</div> -->
	</div>
	<div class="popup_bg"></div>
</div>
<!-- layer_popup_form type_popup : 끝 -->
<script type="text/javascript">
/*==================================================================Start====
 * cfn_confirm 시스템 메세지 박스 출력 [!!!수정금지!!!]
 * 작성자		: 김광일
 * 작성일		: 2013.05
 * 
 * [options filed]
 * type			: Confirm type [YesNo / YesNoCancel / OkCancel]
 * title			: Title text
 * message	: Message text
 * OK			: 확인(OK) 버튼 클릭시, 처리할 이벤트
 * YES			: 예(Yes) 버튼 클릭시, 처리할 이벤트
 * NO			: 아니오(No) 버튼 클릭시, 처리할 이벤트
 * CANCEL	: 취소(Cancel) 버튼 클릭시, 처리할 이벤트
 ==========================================================================*/
function cfn_confirm(options){
// 	 var settings = $.extend({}, {type: "YesNo", objId: "sys_confirmBox"}, options);
	 options = $.extend({}, {type: "YesNo", objId: "sys_confirmBox", focus : ''}, options);
	 var tag = "";
	 // 버튼 설정.
	 $("#"+options.objId+" a.btn_pop_common").hide();
	 switch (options.type) {
	  	case "YesNo":
	  		/* 버튼 보이기 */
	  		tag = "#btn_confirm_yes, #btn_confirm_no";
	  		$(tag).removeClass('btn_custm_class');
	  		$(tag).show();
	  		
	  		/* 버튼 이벤트 등록. */
			$("#btn_confirm_yes").off("click").on("click", function(){
				hideMessageBox($(this));
				if (options.YES&& typeof(options.YES) === "function") { options.YES(); }
			}).trigger("focus");
	  		
			options.focus = 'btn_confirm_yes';
			
			$("#btn_confirm_no").off("click").on("click", function(){
				hideMessageBox($(this));
				if (options.NO && typeof(options.NO) === "function") { options.NO(); }
			});

			

			break;

		case "YesNoCancel":
			tag = "#btn_confirm_yes, #btn_confirm_no, #btn_confirm_cancel";
			$(tag).removeClass('btn_custm_class');
			/* 버튼 보이기 */
			$(tag).show();
			
			/* 버튼 이벤트 등록. */
			$("#btn_confirm_yes").off("click").on("click", function(){
				hideMessageBox($(this));
				if (options.YES&& typeof(options.YES) === "function") { options.YES(); }
			}).trigger("focus");
			
			$("#btn_confirm_no").off("click").on("click", function(){
				hideMessageBox($(this));
				if (options.NO && typeof(options.NO) === "function") { options.NO(); }
			});

			$("#btn_confirm_cancel").off("click").on("click", function(){
				hideMessageBox($(this));
				if (options.CANCEL && typeof(options.CANCEL) === "function") { options.CANCEL(); }
			});
			
			options.focus = 'btn_confirm_yes';
			
			break;
			
		case "OkCancel":
			tag = "#btn_confirm_ok, #btn_confirm_cancel";
			$(tag).removeClass('btn_custm_class');
			/* 버튼 보이기 */
			$(tag).show();

			/* 버튼 이벤트 등록. */
			$("#btn_confirm_ok").off("click").on("click", function(){

				hideMessageBox($(this));
				if (options.OK && typeof(options.OK) === "function") { options.OK(); }
// 				if (options.OK&& typeof(options.OK) === "function") {
// 					if(options.OK()) {
// 						hideMessageBox($(this));
// 					}
// 				}
			}).trigger("focus");
			
			$("#btn_confirm_cancel").off("click").on("click", function(){
				hideMessageBox($(this));
				if (options.CANCEL && typeof(options.CANCEL) === "function") { options.CANCEL(); }
			});
			
			options.focus = 'btn_confirm_ok';
			
			break;

		default:
			break;
	 }
	 
	 // 메세지박스 표시.
	 showMessageBox(options);
	 
	 if(typeof devYN != "undefined" && devYN  == "Y"){
		(function(){
			LANG_MANAGER.initialize({
				"gubun" : "message",
				"tagId" : "sys_confirmBox" 
			});
		})();
 	}

	 $('.btn_pop_message').keydown(function(e) {
		    switch(e.which) {
		        case 37: // left
			        $(tag).removeClass('btn_custm_class');
			        $(tag).prev().focus().addClass('btn_custm_class');;
		        break;	        	
		        case 39: // right
		        	$(tag).removeClass('btn_custm_class');
		        	$(tag).next().focus().addClass('btn_custm_class');;
		        break;
		        default: return; // exit this handler for other keys
		    }
		    e.preventDefault(); // prevent the default action (scroll / move caret)
		});

	 switch (options.type) {
	  	case "YesNo":
			if(options.defaultBtn == "YES"){
				$('#btn_confirm_yes').focus();
				$('#btn_confirm_yes').addClass('btn_custm_class');;
			}else if(options.defaultBtn == "NO"){
				$('#btn_confirm_no').focus();
				$('#btn_confirm_no').addClass('btn_custm_class');;
			}else{
				$('#btn_confirm_yes').focus();
				$('#btn_confirm_yes').addClass('btn_custm_class');;
			}
			break;
		case "YesNoCancel":
			if(options.defaultBtn == "YES"){
				$('#btn_confirm_yes').focus();
				$('#btn_confirm_yes').addClass('btn_custm_class');
			}else if(options.defaultBtn == "NO"){
				$('#btn_confirm_no').focus();
				$('#btn_confirm_no').addClass('btn_custm_class');;
			}else if(options.defaultBtn == "CANCEL"){
				$('#btn_confirm_cancel').focus();
				$('#btn_confirm_cancel').addClass('btn_custm_class');;
			}else{
				$('#btn_confirm_yes').focus();
				$('#btn_confirm_yes').addClass('btn_custm_class');;
			}
			break;
		case "OkCancel":
			/* 버튼 보이기 */
			$("#btn_confirm_ok, #btn_confirm_cancel").show();
			if(options.defaultBtn == "OK"){
				$('#btn_confirm_ok').focus();
				$('#btn_confirm_ok').addClass('btn_custm_class');
			}else if(options.defaultBtn == "CANCEL"){
				$('#btn_confirm_cancel').focus();
				$('#btn_confirm_cancel').addClass('btn_custm_class');;
			}else{
				$('#btn_confirm_ok').focus();
				$('#btn_confirm_ok').addClass('btn_custm_class');;
			}	
			break;
		default:
			break;
	 }	
	 
	 // added prevent showing double times
	 return false;
}
 
 
 function cfn_confirm2(options){
// 	 var settings = $.extend({}, {type: "YesNo", objId: "sys_confirmBox"}, options);
	 options = $.extend({}, {type: "YesNo", objId: "sys_confirmBox2", focus : ''}, options);
	 var tag = "";
	 // 버튼 설정.
	 $("#"+options.objId+" a.btn_pop_common").hide();
	 switch (options.type) {
	  	case "YesNo":
	  		/* 버튼 보이기 */
	  		tag = "#btn_confirm_yes2, #btn_confirm_no2";
	  		$(tag).removeClass('btn_custm_class');
	  		$(tag).show();
	  		
	  		/* 버튼 이벤트 등록. */
			$("#btn_confirm_yes2").off("click").on("click", function(){
				hideMessageBox($(this));
				if (options.YES&& typeof(options.YES) === "function") { options.YES(); }
			}).trigger("focus");
	  		
			options.focus = 'btn_confirm_no2';
			
			$("#btn_confirm_no2").off("click").on("click", function(){
				hideMessageBox($(this));
				if (options.NO && typeof(options.NO) === "function") { options.NO(); }
			});

			

			break;

		case "YesNoCancel":
			tag = "#btn_confirm_ye2, #btn_confirm_no2, #btn_confirm_cancel2";
			$(tag).removeClass('btn_custm_class');
			/* 버튼 보이기 */
			$(tag).show();
			
			/* 버튼 이벤트 등록. */
			$("#btn_confirm_yes2").off("click").on("click", function(){
				hideMessageBox($(this));
				if (options.YES&& typeof(options.YES) === "function") { options.YES(); }
			}).trigger("focus");
			
			$("#btn_confirm_no2").off("click").on("click", function(){
				hideMessageBox($(this));
				if (options.NO && typeof(options.NO) === "function") { options.NO(); }
			});

			$("#btn_confirm_cancel2").off("click").on("click", function(){
				hideMessageBox($(this));
				if (options.CANCEL && typeof(options.CANCEL) === "function") { options.CANCEL(); }
			});
			
			options.focus = 'btn_confirm_no2';
			
			break;
			
		case "OkCancel":
			tag = "#btn_confirm_ok2, #btn_confirm_cancel2";
			$(tag).removeClass('btn_custm_class');
			/* 버튼 보이기 */
			$(tag).show();

			/* 버튼 이벤트 등록. */
			$("#btn_confirm_ok2").off("click").on("click", function(){

				hideMessageBox($(this));
				if (options.OK && typeof(options.OK) === "function") { options.OK(); }
// 				if (options.OK&& typeof(options.OK) === "function") {
// 					if(options.OK()) {
// 						hideMessageBox($(this));
// 					}
// 				}
			}).trigger("focus");
			
			$("#btn_confirm_cancel2").off("click").on("click", function(){
				hideMessageBox($(this));
				if (options.CANCEL && typeof(options.CANCEL) === "function") { options.CANCEL(); }
			});
			
			options.focus = 'btn_confirm_cancel2';
			
			break;

		default:
			break;
	 }
	 
	 // 메세지박스 표시.
	 showMessageBox(options);
	
	 if(typeof devYN != "undefined" && devYN  == "Y"){
		(function(){		
			LANG_MANAGER.initialize({
				"gubun" : "message",
				"tagId" : "sys_confirmBox2"
			});
		})();
	 }

	 $('.btn_pop_message').keydown(function(e) {
		    switch(e.which) {
		        case 37: // left
			        $(tag).removeClass('btn_custm_class');
			        $(tag).prev().focus().addClass('btn_custm_class');;
		        break;	        	
		        case 39: // right
		        	$(tag).removeClass('btn_custm_class');
		        	$(tag).next().focus().addClass('btn_custm_class');;
		        break;
		        default: return; // exit this handler for other keys
		    }
		    e.preventDefault(); // prevent the default action (scroll / move caret)
		});

	 switch (options.type) {
	  	case "YesNo":
			if(options.defaultBtn == "YES"){
				$('#btn_confirm_yes2').focus();
				$('#btn_confirm_yes2').addClass('btn_custm_class');;
			}else if(options.defaultBtn == "NO"){
				$('#btn_confirm_no2').focus();
				$('#btn_confirm_no2').addClass('btn_custm_class');;
			}else{
				$('#btn_confirm_no2').focus();
				$('#btn_confirm_no2').addClass('btn_custm_class');;
			}
			break;
		case "YesNoCancel":
			if(options.defaultBtn == "YES"){
				$('#btn_confirm_yes2').focus();
				$('#btn_confirm_yes2').addClass('btn_custm_class');
			}else if(options.defaultBtn == "NO"){
				$('#btn_confirm_no2').focus();
				$('#btn_confirm_no2').addClass('btn_custm_class');;
			}else if(options.defaultBtn == "CANCEL"){
				$('#btn_confirm_cancel2').focus();
				$('#btn_confirm_cancel2').addClass('btn_custm_class');;
			}else{
				$('#btn_confirm_no2').focus();
				$('#btn_confirm_no2').addClass('btn_custm_class');;
			}
			break;
		case "OkCancel":
			/* 버튼 보이기 */
			$("#btn_confirm_ok, #btn_confirm_cancel").show();
			if(options.defaultBtn == "OK"){
				$('#btn_confirm_ok2').focus();
				$('#btn_confirm_ok2').addClass('btn_custm_class');
			}else if(options.defaultBtn == "CANCEL"){
				$('#btn_confirm_cancel2').focus();
				$('#btn_confirm_cancel2').addClass('btn_custm_class');;
			}else{
				$('#btn_confirm_cancel2').focus();
				$('#btn_confirm_cancel2').addClass('btn_custm_class');;
			}	
			break;
		default:
			break;
	 }	
	 
	 // added prevent showing double times
	 return false;
} 
 /*==================================================================End==== */
</script>
<!--************************************************************************************** 
	메세지박스 - Interactive : End
*****************************************************************************************-->

<script type="text/javascript">

$(document).ready(function(){
	try{
	/*==================================================================Start====
	 * 메세지 박스 상단 닫기 버튼 이벤트 [!!!수정금지!!!]
	 * 작성자		: 김광일
	 * 작성일		: 2013.05
	 ==========================================================================*/
	$('button.btn_popup_close').off('click').on('click', function(){
		hideMessageBox($(this));
	});
	/*==================================================================End==== */

	/*==================================================================Start====
	 * 메세지 박스 드레그 등록 [!!!수정금지!!!]
	 * 작성자		: 김광일
	 * 작성일		: 2013.05
	 ==========================================================================*/
	$(function(){
		$('.popup_form').draggable({ handle: "div[name=popup_handleBar]", containment:"body"});
	});
	/*==================================================================End==== */
	}catch(e){
		console.log(e);
	}
});
</script>