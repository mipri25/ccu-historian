!/**
 * Highstock JS v11.4.0 (2024-03-04)
 *
 * Indicator series type for Highcharts Stock
 *
 * (c) 2010-2024 Rafał Sebestjański
 *
 * License: www.highcharts.com/license
 */function(e){"object"==typeof module&&module.exports?(e.default=e,module.exports=e):"function"==typeof define&&define.amd?define("highcharts/indicators/dema",["highcharts","highcharts/modules/stock"],function(t){return e(t),e.Highcharts=t,e}):e("undefined"!=typeof Highcharts?Highcharts:void 0)}(function(e){"use strict";var t=e?e._modules:{};function s(e,t,s,i){e.hasOwnProperty(t)||(e[t]=i.apply(null,s),"function"==typeof CustomEvent&&window.dispatchEvent(new CustomEvent("HighchartsModuleLoaded",{detail:{path:t,module:e[t]}})))}s(t,"Stock/Indicators/DEMA/DEMAIndicator.js",[t["Core/Series/SeriesRegistry.js"],t["Core/Utilities.js"]],function(e,t){let{ema:s}=e.seriesTypes,{correctFloat:i,isArray:n,merge:o}=t;class r extends s{getEMA(e,t,s,i,n,o){return super.calculateEma(o||[],e,void 0===n?1:n,this.EMApercent,t,void 0===i?-1:i,s)}getValues(e,t){let s=t.period,o=[],r=2*s,a=e.xData,u=e.yData,d=u?u.length:0,c=[],h=[],l=[],p=0,f=0,m,g,y,E,v=-1,M,x=0;if(this.EMApercent=2/(s+1),!(d<2*s-1)){for(n(u[0])&&(v=t.index?t.index:0),x=(p=super.accumulatePeriodPoints(s,v,u))/s,p=0,E=s;E<d+2;E++)E<d+1&&(f=this.getEMA(u,g,x,v,E)[1],o.push(f)),g=f,E<r?p+=f:(E===r&&(x=p/s),f=o[E-s-1],m=this.getEMA([f],y,x)[1],M=[a[E-2],i(2*f-m)],c.push(M),h.push(M[0]),l.push(M[1]),y=m);return{values:c,xData:h,yData:l}}}}return r.defaultOptions=o(s.defaultOptions),e.registerSeriesType("dema",r),r}),s(t,"masters/indicators/dema.src.js",[t["Core/Globals.js"]],function(e){return e})});