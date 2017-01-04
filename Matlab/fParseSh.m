function [bvdid, shname, shbvdid, shctry, shnace, shoprev, shta, shemp, wdir, wtot] = fParseSh(bvdid, shname, shbvdid, shctry, shnace, shoprev, shta, shemp, wdir, wtot)

%% Similar to fParse(...)

bvdid = strrep(bvdid,'"','');

shname = strrep(shname,'"','');
shname = strrep(shname,' via its funds',''); % Orbis stuff we can ignore

shbvdid = strrep(shbvdid,'"','');

shctry = strrep(shctry,'"','');

shnace{1} = '0'; % Header
shnace{2} = '0'; % Header
shnace = strrep(shnace,'"','');
shnace = strrep(shnace,'-','0');
ind = cellfun(@isempty,shnace); % Empty cells
shnace(ind) = {'0'}; % Yeah, Matlab, I know...
shnace = cellfun(@str2num,shnace);

shoprev{1} = '0';
shoprev{2} = '0';
shoprev = strrep(shoprev,'"','');
shoprev = strrep(shoprev,'-','0');
shoprev = strrep(shoprev,',','0');
ind = cellfun(@isempty,shoprev);
shoprev(ind) = {'0'};
shoprev = cellfun(@str2num,shoprev);

shta{1} = '0';
shta{2} = '0';
shta = strrep(shta,'"','');
shta = strrep(shta,'-','0');
shta = strrep(shta,',','0');
ind = cellfun(@isempty,shta);
shta(ind) = {'0'};
shta = cellfun(@str2num,shta);

shemp{1} = '0';
shemp{2} = '0';
shemp = strrep(shemp,'"','');
shemp = strrep(shemp,'-','0');
shemp = strrep(shemp,',','0');
ind = cellfun(@isempty,shemp);
shemp(ind) = {'0'};
shemp = cellfun(@str2num,shemp);

%% Ownership
wdir{1} = '0';
wdir{2} = '0';
wdir = strrep(wdir,'"','');
wdir = strrep(wdir,'-','0');
wdir = strrep(wdir,'NG','0');
wdir = strrep(wdir,'<','');
wdir = strrep(wdir,'>','');
wdir = strrep(wdir,'WO','51');
ind = cellfun(@isempty,wdir);
wdir(ind) = {'0'};
wdir = cellfun(@str2num,wdir);

wtot{1} = '0';
wtot{2} = '0';
wtot = strrep(wtot,'"','');
wtot = strrep(wtot,'-','0');
wtot = strrep(wtot,'NG','0');
wtot = strrep(wtot,'<','');
wtot = strrep(wtot,'>','');
wtot = strrep(wtot,'WO','51');
ind = cellfun(@isempty,wtot);
wtot(ind) = {'0'};
wtot = cellfun(@str2num,wtot);
