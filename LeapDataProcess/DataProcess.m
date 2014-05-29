% Plot the trace data of Leap C++ project, and process them.
% LI ZHEN, April 12th, 2014.
for i = 2:2
    ModelType = 1;                          % 2 types of 3D models
    
    angle_1 = [66 88 0; -66 88 0; 0 88 66];
    angle_2 = [44 105.6 0; -44 105.6 0; 0 105.6 44];
    for r = 1:3
        angle_1(r, :) = angle_1(r, :) ./ sqrt(sum(angle_1(r, :) .^ 2));
        angle_2(r, :) = angle_2(r, :) ./ sqrt(sum(angle_2(r, :) .^ 2));
    end
    angle = cat(3, angle_1, angle_2);
    
    stdEdge = [66 * sqrt(2), 66 * sqrt(2), 132, 110; 44 * sqrt(2), 44 * sqrt(2), 88, 114.4];
    
    dirName = '../LeapTest/LeapTest';
    fileNo = sprintf('3%d0%d.csv', ModelType, i);
    fileName = sprintf('%s/FingerMove%s', dirName, fileNo);
    mat = csvread(fileName, 1, 0, [1 0 1 1]);
    
    % Get current time (ms)
    % Magic Time: 1401178000000ms
    currTime = mat(1) * 1000 + mat(2) - 1401178000000;
    
    % timestamp(us) id1 x1 y1 z1 id2 x2 y2 z2 id3 x3 y3 z3 edge1 edge2 edge3
    % edge1:E12, edge2:E13, edge3:E23
    format long;
    mat = csvread(fileName, 3, 0);
    
    fprintf('%d:\n', i);
    [row col] = size(mat);
    
    plotOriginal = 1;
    plotCenter = 1;
    
    if plotOriginal
        for r = 1:row
            plot3(mat(r, 3), mat(r, 4), mat(r, 5), '.r');
            grid on;
            hold on;
            if mat(r, 6) == 1
                plot3(mat(r, 7), mat(r, 8), mat(r, 9), '.b');
                hold on;
                if mat(r, 10) == 2
                    plot3(mat(r, 11), mat(r, 12), mat(r, 13), '.c');
                    hold on;
                end
            end
        end
    end
    
    if plotCenter
        timeArray = mat(:, 1) / 1000.0 + currTime;
        centerP = GetCenterPos(timeArray, mat(:, 2:13), mat(:, 14:16), ...,
            stdEdge(ModelType, :), angle(:, :, ModelType));
        grid on;
        hold on;
        plot3(centerP(:, 2), centerP(:, 3), centerP(:, 4), '*g');
        
        [pRow pCol] = size(centerP);
        interval = 0.5;                 % interpolate with 0.5ms
        interTime = centerP(1, 1): interval: centerP(pRow, 1);
        interCPos = interp1(centerP(:, 1), centerP(:, 2:4), interTime, 'spline');
        plot3(interCPos(:, 1), interCPos(:, 2), interCPos(:, 3), '.m');
        [iRow iCol] = size(interCPos);
        
        % Get acceleration. convert mm/ms2 to m/s2
        accCenter = diff(interCPos, 2) / interval * 1000;
        [aRow aCol] = size(accCenter);
        
        output = zeros(iRow, 7);
        output(:, 1) = interTime;
        output(:, 2:4) = interCPos;
        output(1:aRow, 5:7) = accCenter;
        
        outFileName = sprintf('%s/CenterPos%s', dirName, fileNo);
        fid = fopen(outFileName, 'w');
        fprintf(fid, 'time(ms), x(mm), y, z, ax(m/s2), ay, az\n');
        fclose(fid);
        dlmwrite(outFileName, output, 'precision', 11, '-append');
    end
end
